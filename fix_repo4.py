import re

with open("app/src/main/java/com/hybrid/messaging/core/data/repository/MessageRepositoryImpl.kt", "r") as f:
    content = f.read()

content = content.replace(
    'import javax.inject.Inject\n',
    'import android.content.Context\nimport androidx.work.Constraints\nimport androidx.work.NetworkType\nimport androidx.work.OneTimeWorkRequestBuilder\nimport androidx.work.WorkManager\nimport dagger.hilt.android.qualifiers.ApplicationContext\nimport com.hybrid.messaging.core.network.worker.MessageSyncWorker\nimport javax.inject.Inject\n'
)

content = content.replace(
    '    private val webSocketManager: WebSocketManager\n',
    '    private val webSocketManager: WebSocketManager,\n    @ApplicationContext private val context: Context\n'
)

content = content.replace(
    'class MessageRepositoryImpl @Inject constructor(\n',
    'class MessageRepositoryImpl @Inject constructor(\n'
)

# Text message enqueue
content = content.replace(
    '        if (result.isSuccess) {\n            messageDao.updateMessageSyncState(messageId, SyncState.SENT)\n        } else {\n            messageDao.updateMessageSyncState(messageId, SyncState.FAILED)\n        }\n\n        val domainMessage = Message(\n            id = messageId,\n            roomId = roomId,\n            senderId = currentUserId,\n            senderName = currentUserName,\n            content = text,\n            messageType = MessageType.TEXT,\n            timestamp = timestamp,\n            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n            syncState = if (result.isSuccess) SyncState.SENT else SyncState.FAILED,\n            replyToMessageId = replyToId\n        )',
    '        if (result.isSuccess) {\n            messageDao.updateMessageSyncState(messageId, SyncState.SENT)\n        } else {\n            messageDao.updateMessageSyncState(messageId, SyncState.FAILED)\n            enqueueSyncWorker()\n        }\n\n        val domainMessage = Message(\n            id = messageId,\n            roomId = roomId,\n            senderId = currentUserId,\n            senderName = currentUserName,\n            content = text,\n            messageType = MessageType.TEXT,\n            timestamp = timestamp,\n            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n            syncState = if (result.isSuccess) SyncState.SENT else SyncState.FAILED,\n            replyToMessageId = replyToId\n        )'
)

# Voice note enqueue
content = content.replace(
    '        if (result.isSuccess) {\n            messageDao.updateMessageSyncState(messageId, SyncState.SENT)\n        } else {\n            messageDao.updateMessageSyncState(messageId, SyncState.FAILED)\n        }\n\n        return Resource.Success(',
    '        if (result.isSuccess) {\n            messageDao.updateMessageSyncState(messageId, SyncState.SENT)\n        } else {\n            messageDao.updateMessageSyncState(messageId, SyncState.FAILED)\n            enqueueSyncWorker()\n        }\n\n        return Resource.Success('
)

# Add enqueueSyncWorker function
content = content.replace(
    '    override suspend fun markRoomAsRead(roomId: String): Resource<Unit> {\n        return Resource.Success(Unit)\n    }\n}',
    '    override suspend fun markRoomAsRead(roomId: String): Resource<Unit> {\n        return Resource.Success(Unit)\n    }\n\n    private fun enqueueSyncWorker() {\n        val constraints = Constraints.Builder()\n            .setRequiredNetworkType(NetworkType.CONNECTED)\n            .build()\n        val workRequest = OneTimeWorkRequestBuilder<MessageSyncWorker>()\n            .setConstraints(constraints)\n            .build()\n        WorkManager.getInstance(context).enqueue(workRequest)\n    }\n}'
)

with open("app/src/main/java/com/hybrid/messaging/core/data/repository/MessageRepositoryImpl.kt", "w") as f:
    f.write(content)
