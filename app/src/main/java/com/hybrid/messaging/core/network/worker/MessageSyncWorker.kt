package com.hybrid.messaging.core.network.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hybrid.messaging.core.database.dao.MessageDao
import com.hybrid.messaging.core.model.SyncState
import com.hybrid.messaging.core.network.websocket.SocketFrame
import com.hybrid.messaging.core.network.websocket.WebSocketManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MessageSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageDao: MessageDao,
    private val webSocketManager: WebSocketManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val pendingMessages = messageDao.getUnsyncedMessages()
            var allSuccessful = true

            for (message in pendingMessages) {
                // Attempt to send
                runCatching {
                    webSocketManager.sendFrame(
                        SocketFrame.MessagePayload(
                            id = message.id,
                            roomId = message.roomId,
                            senderId = message.senderId,
                            senderName = message.senderName,
                            content = message.content,
                            messageType = message.messageType.name,
                            mediaUrl = message.mediaUrl,
                            audioDurationMs = message.audioDurationMs,
                            timestamp = message.timestamp,
                            encryptionStatus = message.encryptionStatus.name
                        )
                    )
                }.onSuccess {
                    messageDao.updateMessageSyncState(message.id, SyncState.SENT)
                }.onFailure {
                    // Fail gracefully and leave it PENDING for next retry
                    allSuccessful = false
                }
            }

            if (allSuccessful) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
