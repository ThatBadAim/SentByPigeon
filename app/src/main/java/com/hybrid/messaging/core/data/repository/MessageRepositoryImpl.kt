package com.hybrid.messaging.core.data.repository

import com.hybrid.messaging.core.database.dao.MessageDao
import com.hybrid.messaging.core.database.dao.ReactionDao
import com.hybrid.messaging.core.database.entity.MessageEntity
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
import com.hybrid.messaging.core.worker.MessageSyncWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val reactionDao: ReactionDao,
    private val webSocketManager: WebSocketManager,
    @ApplicationContext private val context: Context
) : MessageRepository {

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
                    syncState = entity.syncState,
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

        val result = runCatching {
            webSocketManager.sendFrame(
                SocketFrame.MessagePayload(
                    id = messageId,
                    roomId = roomId,
                    senderId = currentUserId,
                    senderName = currentUserName,
                    content = text,
                    messageType = MessageType.TEXT.name,
                    timestamp = timestamp,
                    encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3.name
                )
            )
        }

        if (result.isSuccess) {
            messageDao.updateMessageSyncState(messageId, SyncState.SENT)
        } else {
            enqueueSyncWorker()
        }

        val domainMessage = Message(
            id = messageId,
            roomId = roomId,
            senderId = currentUserId,
            senderName = currentUserName,
            content = text,
            messageType = MessageType.TEXT,
            timestamp = timestamp,
            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,
            syncState = if (result.isSuccess) SyncState.SENT else SyncState.PENDING,
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

        val result = runCatching {
            webSocketManager.sendFrame(
                SocketFrame.MessagePayload(
                    id = messageId,
                    roomId = roomId,
                    senderId = currentUserId,
                    senderName = currentUserName,
                    content = entity.content,
                    messageType = MessageType.VOICE_NOTE.name,
                    mediaUrl = audioFilePath,
                    audioDurationMs = durationMs,
                    timestamp = timestamp,
                    encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3.name
                )
            )
        }

        if (result.isSuccess) {
            messageDao.updateMessageSyncState(messageId, SyncState.SENT)
        } else {
            enqueueSyncWorker()
        }

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
                encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,
                syncState = if (result.isSuccess) SyncState.SENT else SyncState.PENDING
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

        val result = runCatching {
            webSocketManager.sendFrame(
                SocketFrame.MessagePayload(
                    id = messageId,
                    roomId = roomId,
                    senderId = currentUserId,
                    senderName = currentUserName,
                    content = entity.content,
                    messageType = type.name,
                    mediaUrl = mediaUrl,
                    timestamp = timestamp,
                    encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3.name
                )
            )
        }

        if (result.isSuccess) {
            messageDao.updateMessageSyncState(messageId, SyncState.SENT)
        } else {
            enqueueSyncWorker()
        }

        return Resource.Success(
            Message(
                id = messageId,
                roomId = roomId,
                senderId = currentUserId,
                senderName = currentUserName,
                content = "Attachment",
                messageType = type,
                mediaUrl = mediaUrl,
                timestamp = timestamp,
                encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,
                syncState = if (result.isSuccess) SyncState.SENT else SyncState.PENDING
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

    private fun enqueueSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MessageSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
