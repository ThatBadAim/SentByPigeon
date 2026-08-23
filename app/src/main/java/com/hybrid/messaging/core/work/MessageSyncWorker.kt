package com.hybrid.messaging.core.work

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
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageDao: MessageDao,
    private val webSocketManager: WebSocketManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val pendingMessages = messageDao.getPendingMessages().firstOrNull() ?: emptyList()
        if (pendingMessages.isEmpty()) {
            return Result.success()
        }

        var hasFailure = false

        for (message in pendingMessages) {
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
                messageDao.updateSyncState(message.id, SyncState.SENT)
            } catch (e: Exception) {
                hasFailure = true
            }
        }

        return if (hasFailure) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}
