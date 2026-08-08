package com.hybrid.messaging.core.data.repository

import com.hybrid.messaging.core.database.dao.MessageDao
import com.hybrid.messaging.core.database.dao.ReactionDao
import com.hybrid.messaging.core.database.entity.MessageEntity
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.hybrid.messaging.core.database.entity.ReactionEntity
import com.hybrid.messaging.core.domain.repository.MessageRepository
import com.hybrid.messaging.core.domain.util.Resource
import com.hybrid.messaging.core.model.EncryptionStatus
import com.hybrid.messaging.core.model.Message
import com.hybrid.messaging.core.model.MessageType
import com.hybrid.messaging.core.model.Reaction
import com.hybrid.messaging.core.model.SyncState
import com.hybrid.messaging.core.network.websocket.SocketFrame
import com.hybrid.messaging.core.network.websocket.WebSocketManager
import com.hybrid.messaging.core.network.worker.MessageSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val reactionDao: ReactionDao,
    private val webSocketManager: WebSocketManager,
    private val workManager: WorkManager
) : MessageRepository {

    private fun enqueueSyncJob() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<MessageSyncWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork("MessageSync", ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    override fun getMessagesForRoom(roomId: String): Flow<List<Message>> {
        return messageDao.getMessagesForRoom(roomId).map { entities ->
            entities.map { entity ->
                Message(
                    id = entity.id,
                    roomId = entity.roomId,
                    senderId = entity.senderId,
                    senderName = entity.senderName,
                    senderAvatarUrl = entity.senderAvatarUrl,
                    content = entity.content,
                    messageType = entity.messageType,
                    mediaUrl = entity.mediaUrl,
                    audioDurationMs = entity.audioDurationMs,
                    timestamp = entity.timestamp,
                    encryptionStatus = entity.encryptionStatus,
                    reactions = emptyList(),
                    replyToMessageId = entity.replyToMessageId
                )
            }
        }
    }

    override suspend fun sendTextMessage(
        roomId: String,
        text: String,
        replyToId: String?
    ): Resource<Message> {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val currentUserId = "user_me"
        val currentUserName = "Alex Mercer"

        val entity = MessageEntity(
            id = messageId,
            roomId = roomId,
            senderId = currentUserId,
            senderName = currentUserName,
            senderAvatarUrl = null,
            content = text,
            messageType = MessageType.TEXT,
            mediaUrl = null,
            audioDurationMs = null,
            timestamp = timestamp,
            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,
            syncState = SyncState.PENDING,
            replyToMessageId = replyToId
        )

        messageDao.insertMessage(entity)
        enqueueSyncJob()

        val domainMessage = Message(
            id = messageId,
            roomId = roomId,
            senderId = currentUserId,
            senderName = currentUserName,
            content = text,
            messageType = MessageType.TEXT,
            timestamp = timestamp,
            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,
            replyToMessageId = replyToId
        )

        return Resource.Success(domainMessage)
    }

    override suspend fun sendVoiceNote(
        roomId: String,
        audioFilePath: String,
        durationMs: Long
    ): Resource<Message> {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val currentUserId = "user_me"
        val currentUserName = "Alex Mercer"

        val entity = MessageEntity(
            id = messageId,
            roomId = roomId,
            senderId = currentUserId,
            senderName = currentUserName,
            senderAvatarUrl = null,
            content = "Voice note (${durationMs / 1000}s)",
            messageType = MessageType.VOICE_NOTE,
            mediaUrl = audioFilePath,
            audioDurationMs = durationMs,
            timestamp = timestamp,
            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,
            syncState = SyncState.PENDING,
            replyToMessageId = null
        )

        messageDao.insertMessage(entity)
        enqueueSyncJob()

        return Resource.Success(
            Message(
                id = messageId,
                roomId = roomId,
                senderId = currentUserId,
                senderName = currentUserName,
                content = entity.content,
                messageType = MessageType.VOICE_NOTE,
                mediaUrl = audioFilePath,
                audioDurationMs = durationMs,
                timestamp = timestamp,
                encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3
            )
        )
    }

    override suspend fun sendMediaMessage(
        roomId: String,
        mediaUrl: String,
        type: MessageType
    ): Resource<Message> {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val currentUserId = "user_me"
        val currentUserName = "Alex Mercer"

        val entity = MessageEntity(
            id = messageId,
            roomId = roomId,
            senderId = currentUserId,
            senderName = currentUserName,
            senderAvatarUrl = null,
            content = "Attachment",
            messageType = type,
            mediaUrl = mediaUrl,
            audioDurationMs = null,
            timestamp = timestamp,
            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,
            syncState = SyncState.PENDING,
            replyToMessageId = null
        )

        messageDao.insertMessage(entity)
        enqueueSyncJob()

        return Resource.Success(
            Message(
                id = messageId,
                roomId = roomId,
                senderId = currentUserId,
                senderName = currentUserName,
                content = "Attachment",
                messageType = type,
                mediaUrl = mediaUrl,
                timestamp = timestamp
            )
        )
    }

    override suspend fun addReaction(messageId: String, emoji: String): Resource<Unit> {
        val currentUserId = "user_me"
        reactionDao.insertReaction(
            ReactionEntity(
                messageId = messageId,
                emoji = emoji,
                userId = currentUserId
            )
        )
        return Resource.Success(Unit)
    }

    override suspend fun removeReaction(messageId: String, emoji: String): Resource<Unit> {
        val currentUserId = "user_me"
        reactionDao.removeReaction(messageId, emoji, currentUserId)
        return Resource.Success(Unit)
    }

    override suspend fun markRoomAsRead(roomId: String): Resource<Unit> {
        return Resource.Success(Unit)
    }
}
