sed -i 's/import kotlinx.coroutines.cancel//g' app/src/main/java/com/hybrid/messaging/core/network/websocket/WebSocketManager.kt
sed -i 's/import io.ktor.websocket.close//g' app/src/main/java/com/hybrid/messaging/core/network/websocket/WebSocketManager.kt
sed -i 's/session?.cancel()/session?.cancel()/g' app/src/main/java/com/hybrid/messaging/core/network/websocket/WebSocketManager.kt
