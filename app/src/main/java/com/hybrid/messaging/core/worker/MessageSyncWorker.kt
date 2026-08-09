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
    @Assisted params: WorkerParameters,
    private val messageDao: MessageDao,
    private val webSocketManager: WebSocketManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val pendingMessages = messageDao.getPendingMessages().firstOrNull() ?: emptyList()

            if (pendingMessages.isEmpty()) {
                return Result.success()
            }

            pendingMessages.forEach { entity ->
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

                    // Optimistic update: assuming successful send if it didn't throw
                    messageDao.insertMessage(entity.copy(syncState = SyncState.SENT))
                } catch (e: Exception) {
                    // Fail gracefully on individual messages to retry later
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
