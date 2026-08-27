package com.hybrid.messaging.core.network.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hybrid.messaging.core.database.dao.MessageDao
import com.hybrid.messaging.core.network.websocket.WebSocketManager
import com.hybrid.messaging.core.network.websocket.SocketFrame
import com.hybrid.messaging.core.model.SyncState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class MessageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageDao: MessageDao,
    private val webSocketManager: WebSocketManager
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            val unsyncedMessages = messageDao.getUnsyncedMessages()
            var hasFailures = false

            for (message in unsyncedMessages) {
                try {
                    webSocketManager.sendFrame(
                        SocketFrame.MessagePayload(
                            id = message.id,
                            roomId = message.roomId,
                            senderId = message.senderId,
                            senderName = message.senderName,
                            content = message.content,
                            messageType = message.messageType.name,
                            timestamp = message.timestamp,
                            encryptionStatus = message.encryptionStatus.name
                        )
                    )
                    messageDao.updateMessageSyncState(message.id, SyncState.SENT)
                } catch (e: Exception) {
                    messageDao.updateMessageSyncState(message.id, SyncState.FAILED)
                    hasFailures = true
                }
            }

            if (hasFailures) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
