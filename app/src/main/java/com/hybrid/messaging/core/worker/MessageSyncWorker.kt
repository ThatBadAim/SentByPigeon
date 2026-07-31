package com.hybrid.messaging.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hybrid.messaging.core.database.dao.MessageDao
import com.hybrid.messaging.core.model.EncryptionStatus
import com.hybrid.messaging.core.model.SyncState
import com.hybrid.messaging.core.network.websocket.SocketFrame
import com.hybrid.messaging.core.network.websocket.WebSocketManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class MessageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageDao: MessageDao,
    private val webSocketManager: WebSocketManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingMessages = messageDao.getMessagesBySyncState(SyncState.PENDING).firstOrNull() ?: emptyList()

        if (pendingMessages.isEmpty()) {
            return Result.success()
        }

        var allSuccessful = true

        for (entity in pendingMessages) {
            try {
                webSocketManager.sendFrame(
                    SocketFrame.MessagePayload(
                        id = entity.id,
                        roomId = entity.roomId,
                        senderId = entity.senderId,
                        senderName = entity.senderName,
                        content = entity.content,
                        messageType = entity.messageType.name,
                        mediaUrl = entity.mediaUrl,
                        audioDurationMs = entity.audioDurationMs,
                        timestamp = entity.timestamp,
                        encryptionStatus = entity.encryptionStatus.name
                    )
                )
                messageDao.updateMessageSyncState(entity.id, SyncState.SENT)
            } catch (e: Exception) {
                allSuccessful = false
                // Note: We leave it as PENDING if it fails, so it will be retried
            }
        }

        return if (allSuccessful) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}