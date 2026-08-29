package com.hybrid.messaging.core.data.repository

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class MessageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageDao: MessageDao,
    private val webSocketManager: WebSocketManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val unsentMessages = messageDao.getUnsentMessagesList()

            if (unsentMessages.isEmpty()) {
                return@withContext Result.success()
            }

            var allSuccess = true

            for (entity in unsentMessages) {
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
                    allSuccess = false
                    messageDao.updateMessageSyncState(entity.id, SyncState.FAILED)
                }
            }

            if (allSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
