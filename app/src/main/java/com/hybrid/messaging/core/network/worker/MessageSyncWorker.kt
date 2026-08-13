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
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class MessageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageDao: MessageDao,
    private val webSocketManager: WebSocketManager
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val pendingMessages = messageDao.getPendingMessages().firstOrNull() ?: return Result.success()

        var hasFailure = false
        for (entity in pendingMessages) {
            val result = runCatching {
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
            }

            if (result.isSuccess) {
                messageDao.updateMessageSyncState(entity.id, SyncState.SENT)
            } else {
                messageDao.updateMessageSyncState(entity.id, SyncState.FAILED)
                hasFailure = true
            }
        }

        return if (hasFailure) Result.retry() else Result.success()
    }
}
