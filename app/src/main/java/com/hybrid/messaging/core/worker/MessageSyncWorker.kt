package com.hybrid.messaging.core.worker

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
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class MessageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageDao: MessageDao,
    private val webSocketManager: WebSocketManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val pendingMessages = messageDao.getPendingMessages().firstOrNull() ?: emptyList()

            var allSent = true

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
                            timestamp = entity.timestamp,
                            encryptionStatus = entity.encryptionStatus.name
                        )
                    )
                    messageDao.updateMessageSyncState(entity.id, SyncState.SENT)
                } catch (e: Exception) {
                    allSent = false
                    // Keep the message as PENDING so it gets picked up on the next retry.
                }
            }

            if (allSent) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}