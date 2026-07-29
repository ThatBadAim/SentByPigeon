package com.hybrid.messaging.core.domain.repository

import com.hybrid.messaging.core.domain.util.Resource
import com.hybrid.messaging.core.model.CallSession
import com.hybrid.messaging.core.model.ChatRoom
import com.hybrid.messaging.core.model.Message
import com.hybrid.messaging.core.model.MessageType
import com.hybrid.messaging.core.model.Server
import com.hybrid.messaging.core.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun registerWithPhoneNumber(phoneNumber: String, username: String): Resource<User>
    suspend fun verifyOtp(phoneNumber: String, code: String): Resource<User>
    suspend fun updateE2eeIdentityKeys(publicKeyFingerprint: String): Resource<Unit>
    suspend fun logout()
}

interface MessageRepository {
    fun getMessagesForRoom(roomId: String): Flow<List<Message>>
    suspend fun sendTextMessage(roomId: String, text: String, replyToId: String? = null): Resource<Message>
    suspend fun sendVoiceNote(roomId: String, audioFilePath: String, durationMs: Long): Resource<Message>
    suspend fun sendMediaMessage(roomId: String, mediaUrl: String, type: MessageType): Resource<Message>
    suspend fun addReaction(messageId: String, emoji: String): Resource<Unit>
    suspend fun removeReaction(messageId: String, emoji: String): Resource<Unit>
    suspend fun markRoomAsRead(roomId: String): Resource<Unit>
    suspend fun searchMessages(query: String): Resource<List<Message>>
}

interface ChatRoomRepository {
    fun getDirectAndGroupChats(): Flow<List<ChatRoom>>
    fun getChatRoom(roomId: String): Flow<ChatRoom?>
    suspend fun createDirectMessage(targetUserId: String): Resource<ChatRoom>
    suspend fun createGroupChat(name: String, memberIds: List<String>): Resource<ChatRoom>
}

interface ServerRepository {
    fun getServers(): Flow<List<Server>>
    fun getServerDetails(serverId: String): Flow<Server?>
    suspend fun createServer(name: String, iconUrl: String?): Resource<Server>
    suspend fun createCategory(serverId: String, name: String): Resource<Unit>
    suspend fun createChannel(serverId: String, categoryId: String?, name: String, isVoice: Boolean): Resource<ChatRoom>
}

interface WebRtcRepository {
    val activeCallSession: Flow<CallSession?>
    suspend fun initiateCall(roomId: String, isVideo: Boolean): Resource<CallSession>
    suspend fun acceptCall(callId: String): Resource<Unit>
    suspend fun rejectCall(callId: String): Resource<Unit>
    suspend fun endCall(callId: String): Resource<Unit>
}
