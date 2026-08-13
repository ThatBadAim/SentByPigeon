import re

with open("app/src/main/java/com/hybrid/messaging/core/data/repository/MessageRepositoryImpl.kt", "r") as f:
    content = f.read()

content = content.replace(
    'import com.hybrid.messaging.core.model.Reaction\n',
    'import com.hybrid.messaging.core.model.Reaction\nimport com.hybrid.messaging.core.model.SyncState\n'
)

content = content.replace(
    '                    encryptionStatus = entity.encryptionStatus,\n                    reactions = emptyList(),',
    '                    encryptionStatus = entity.encryptionStatus,\n                    syncState = entity.syncState,\n                    reactions = emptyList(),'
)

# Text message
content = content.replace(
    '            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n            replyToMessageId = replyToId\n        )\n\n        messageDao.insertMessage(entity)\n\n        runCatching {\n            webSocketManager.sendFrame(',
    '            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n            syncState = SyncState.PENDING,\n            replyToMessageId = replyToId\n        )\n\n        messageDao.insertMessage(entity)\n\n        val result = runCatching {\n            webSocketManager.sendFrame('
)

content = content.replace(
    '                )\n            )\n        }\n\n        val domainMessage = Message(',
    '                )\n            )\n        }\n\n        if (result.isSuccess) {\n            messageDao.updateMessageSyncState(messageId, SyncState.SENT)\n        } else {\n            messageDao.updateMessageSyncState(messageId, SyncState.FAILED)\n        }\n\n        val domainMessage = Message('
)

content = content.replace(
    '            messageType = MessageType.TEXT,\n            timestamp = timestamp,\n            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n            replyToMessageId = replyToId\n        )',
    '            messageType = MessageType.TEXT,\n            timestamp = timestamp,\n            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n            syncState = if (result.isSuccess) SyncState.SENT else SyncState.FAILED,\n            replyToMessageId = replyToId\n        )'
)

# Voice note
content = content.replace(
    '            timestamp = timestamp,\n            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n            replyToMessageId = null\n        )\n\n        messageDao.insertMessage(entity)\n\n        return Resource.Success(',
    '            timestamp = timestamp,\n            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n            syncState = SyncState.PENDING,\n            replyToMessageId = null\n        )\n\n        messageDao.insertMessage(entity)\n\n        val result = runCatching {\n            webSocketManager.sendFrame(\n                SocketFrame.MessagePayload(\n                    id = messageId,\n                    roomId = roomId,\n                    senderId = currentUserId,\n                    senderName = currentUserName,\n                    content = entity.content,\n                    messageType = MessageType.VOICE_NOTE.name,\n                    mediaUrl = audioFilePath,\n                    audioDurationMs = durationMs,\n                    timestamp = timestamp,\n                    encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3.name\n                )\n            )\n        }\n\n        if (result.isSuccess) {\n            messageDao.updateMessageSyncState(messageId, SyncState.SENT)\n        } else {\n            messageDao.updateMessageSyncState(messageId, SyncState.FAILED)\n        }\n\n        return Resource.Success('
)

content = content.replace(
    '                audioDurationMs = durationMs,\n                timestamp = timestamp,\n                encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3\n            )\n        )',
    '                audioDurationMs = durationMs,\n                timestamp = timestamp,\n                encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n                syncState = if (result.isSuccess) SyncState.SENT else SyncState.FAILED\n            )\n        )'
)


# Media Message
content = content.replace(
    '            audioDurationMs = null,\n            timestamp = timestamp,\n            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n            replyToMessageId = null\n        )\n\n        messageDao.insertMessage(entity)\n\n        return Resource.Success(',
    '            audioDurationMs = null,\n            timestamp = timestamp,\n            encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n            syncState = SyncState.PENDING,\n            replyToMessageId = null\n        )\n\n        messageDao.insertMessage(entity)\n\n        val result = runCatching {\n            webSocketManager.sendFrame(\n                SocketFrame.MessagePayload(\n                    id = messageId,\n                    roomId = roomId,\n                    senderId = currentUserId,\n                    senderName = currentUserName,\n                    content = entity.content,\n                    messageType = type.name,\n                    mediaUrl = mediaUrl,\n                    timestamp = timestamp,\n                    encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3.name\n                )\n            )\n        }\n\n        if (result.isSuccess) {\n            messageDao.updateMessageSyncState(messageId, SyncState.SENT)\n        } else {\n            messageDao.updateMessageSyncState(messageId, SyncState.FAILED)\n        }\n\n        return Resource.Success('
)

content = content.replace(
    '                messageType = type,\n                mediaUrl = mediaUrl,\n                timestamp = timestamp\n            )\n        )',
    '                messageType = type,\n                mediaUrl = mediaUrl,\n                timestamp = timestamp,\n                syncState = if (result.isSuccess) SyncState.SENT else SyncState.FAILED,\n                encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3\n            )\n        )'
)

with open("app/src/main/java/com/hybrid/messaging/core/data/repository/MessageRepositoryImpl.kt", "w") as f:
    f.write(content)
