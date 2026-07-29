package com.hybrid.messaging.core.network.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class SocketFrame {
    @Serializable
    @SerialName("ping")
    data object Ping : SocketFrame()

    @Serializable
    @SerialName("pong")
    data object Pong : SocketFrame()

    @Serializable
    @SerialName("message")
    data class MessagePayload(
        val id: String,
        val roomId: String,
        val senderId: String,
        val senderName: String,
        val content: String,
        val messageType: String,
        val mediaUrl: String? = null,
        val audioDurationMs: Long? = null,
        val timestamp: Long,
        val encryptionStatus: String
    ) : SocketFrame()

    @Serializable
    @SerialName("typing")
    data class TypingPayload(
        val roomId: String,
        val userId: String,
        val isTyping: Boolean
    ) : SocketFrame()

    @Serializable
    @SerialName("read_receipt")
    data class ReadReceiptPayload(
        val roomId: String,
        val messageId: String,
        val userId: String,
        val timestamp: Long
    ) : SocketFrame()

    @Serializable
    @SerialName("reaction")
    data class ReactionPayload(
        val messageId: String,
        val emoji: String,
        val userId: String,
        val isAdded: Boolean
    ) : SocketFrame()

    @Serializable
    @SerialName("webrtc_signal")
    data class WebRtcSignalPayload(
        val callId: String,
        val senderId: String,
        val sdpType: String,
        val sdpData: String,
        val iceCandidate: String? = null
    ) : SocketFrame()
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING
}
