package com.hybrid.messaging.core.model

import java.util.Date

/**
 * Domain representation of a User in the system.
 */
data class User(
    val id: String,
    val phoneNumber: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val status: UserStatus = UserStatus.OFFLINE,
    val statusCustomMessage: String? = null,
    val publicKeyFingerprint: String? = null,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

enum class UserStatus {
    ONLINE, AWAY, BUSY, OFFLINE
}

/**
 * ChatRoom represents both WhatsApp/iMessage direct/group chats
 * as well as Discord-style Space Text & Voice Channels.
 */
data class ChatRoom(
    val id: String,
    val type: ChatRoomType,
    val name: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val serverId: String? = null,
    val categoryId: String? = null,
    val isEncrypted: Boolean = true,
    val encryptionKeyId: String? = null,
    val unreadCount: Int = 0,
    val lastMessage: Message? = null,
    val memberIds: List<String> = emptyList()
)

enum class ChatRoomType {
    DIRECT_MESSAGE,
    GROUP_CHAT,
    SPACE_TEXT_CHANNEL,
    SPACE_VOICE_ROOM
}

/**
 * Server (Space) represents a Discord-like community server containing nested channels.
 */
data class Server(
    val id: String,
    val name: String,
    val iconUrl: String? = null,
    val ownerId: String,
    val categories: List<ChannelCategory> = emptyList(),
    val members: List<User> = emptyList(),
    val roles: List<Role> = emptyList()
)

data class ChannelCategory(
    val id: String,
    val serverId: String,
    val name: String,
    val position: Int,
    val channels: List<ChatRoom> = emptyList()
)

data class Role(
    val id: String,
    val serverId: String,
    val name: String,
    val colorHex: String = "#808080",
    val permissions: Set<RolePermission> = emptySet()
)

enum class RolePermission {
    ADMINISTRATOR,
    MANAGE_CHANNELS,
    MANAGE_ROLES,
    SEND_MESSAGES,
    ATTACH_FILES,
    CONNECT_VOICE,
    MUTE_MEMBERS
}

/**
 * Represents a single message payload in a room or channel.
 */
data class Message(
    val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatarUrl: String? = null,
    val content: String,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val audioDurationMs: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val encryptionStatus: EncryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,
    val syncState: SyncState = SyncState.PENDING,
    val reactions: List<Reaction> = emptyList(),
    val readReceipts: List<ReadReceipt> = emptyList(),
    val replyToMessageId: String? = null
)

enum class MessageType {
    TEXT, IMAGE, VOICE_NOTE, CALL_LOG, SYSTEM
}

enum class EncryptionStatus {
    UNENCRYPTED, PENDING, ENCRYPTED_SIGNAL_V3, VERIFIED
}

enum class SyncState {
    PENDING, SENT, DELIVERED, READ, FAILED
}

/**
 * Reaction overlay model for iMessage/Discord style emoji reactions.
 */
data class Reaction(
    val emoji: String,
    val count: Int,
    val userIds: List<String>,
    val containsCurrentUser: Boolean = false
)

data class ReadReceipt(
    val userId: String,
    val timestamp: Long,
    val isRead: Boolean = true
)

/**
 * WebRTC call session model.
 */
data class CallSession(
    val callId: String,
    val roomId: String,
    val callerId: String,
    val isVideoCall: Boolean,
    val state: CallState = CallState.IDLE,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CallState {
    IDLE, DIALING, RINGING, CONNECTED, ENDED
}
