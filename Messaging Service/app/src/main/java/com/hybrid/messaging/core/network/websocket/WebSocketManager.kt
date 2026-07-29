package com.hybrid.messaging.core.network.websocket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

@Singleton
class WebSocketManager @Inject constructor(
    private val client: HttpClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingEvents = MutableSharedFlow<SocketFrame>(extraBufferCapacity = 100)
    val incomingEvents: SharedFlow<SocketFrame> = _incomingEvents.asSharedFlow()

    private var session: DefaultClientWebSocketSession? = null
    private val sendMutex = Mutex()

    private var connectionJob: Job? = null
    private var heartbeatJob: Job? = null
    private val reconnectAttempt = AtomicInteger(0)

    private var currentUrl: String = "wss://api.nexus-messaging.internal/v1/ws"

    fun connect(serverUrl: String = currentUrl) {
        currentUrl = serverUrl
        if (_connectionState.value == ConnectionState.CONNECTED || _connectionState.value == ConnectionState.CONNECTING) {
            return
        }

        connectionJob?.cancel()
        connectionJob = scope.launch {
            establishConnectionWithRetry()
        }
    }

    private suspend fun establishConnectionWithRetry() {
        while (scope.isActive) {
            try {
                _connectionState.value = if (reconnectAttempt.get() > 0) ConnectionState.RECONNECTING else ConnectionState.CONNECTING

                session = client.webSocketSession {
                    url(currentUrl)
                }

                _connectionState.value = ConnectionState.CONNECTED
                reconnectAttempt.set(0)

                startHeartbeat()
                listenIncomingFrames()

            } catch (e: Exception) {
                session = null
                stopHeartbeat()

                _connectionState.value = ConnectionState.DISCONNECTED
                val attempts = reconnectAttempt.incrementAndGet()
                val backoffMs = calculateExponentialBackoff(attempts)

                delay(backoffMs)
            }
        }
    }

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = scope.launch {
            while (isActive && _connectionState.value == ConnectionState.CONNECTED) {
                delay(25_000) // 25s ping interval
                try {
                    sendFrame(SocketFrame.Ping)
                } catch (e: Exception) {
                    // Force reconnect on heartbeat failure
                    session?.cancel()
                    break
                }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private suspend fun listenIncomingFrames() {
        val currentSession = session ?: return
        try {
            for (frame in currentSession.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    runCatching {
                        val parsedFrame = json.decodeFromString<SocketFrame>(text)
                        if (parsedFrame is SocketFrame.Pong) {
                            // Heartbeat response acknowledged
                        } else {
                            _incomingEvents.emit(parsedFrame)
                        }
                    }
                }
            }
        } finally {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    suspend fun sendFrame(frame: SocketFrame) {
        sendMutex.withLock {
            val currentSession = session
            if (currentSession != null && _connectionState.value == ConnectionState.CONNECTED) {
                val serializedJson = json.encodeToString(frame)
                currentSession.send(Frame.Text(serializedJson))
            } else {
                throw IllegalStateException("WebSocket is not connected. Current state: ${_connectionState.value}")
            }
        }
    }

    fun disconnect() {
        scope.launch {
            stopHeartbeat()
            connectionJob?.cancel()
            connectionJob = null
            session?.cancel()
            session = null
            _connectionState.value = ConnectionState.DISCONNECTED
            reconnectAttempt.set(0)
        }
    }

    private fun calculateExponentialBackoff(attempt: Int): Long {
        val maxDelayMs = 30_000L
        val baseDelayMs = 1_000L
        val delayMs = baseDelayMs * (2.0.pow(attempt.toDouble())).toLong()
        return min(delayMs, maxDelayMs)
    }
}
