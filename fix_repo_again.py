import re

with open("app/src/main/java/com/hybrid/messaging/core/data/repository/MessageRepositoryImpl.kt", "r") as f:
    content = f.read()

# Fix the mediaUrl issue in sendMediaMessage where it replaced it with VOICE_NOTE.name and audioFilePath
content = content.replace(
    '                    messageType = MessageType.VOICE_NOTE.name,\n                    mediaUrl = audioFilePath,\n                    audioDurationMs = durationMs,\n                    timestamp = timestamp,\n                    encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3.name\n                )\n            )\n        }\n\n        if (result.isSuccess) {\n            messageDao.updateMessageSyncState(messageId, SyncState.SENT)\n        } else {\n            messageDao.updateMessageSyncState(messageId, SyncState.FAILED)\n        }\n\n        return Resource.Success(\n            Message(\n                id = messageId,\n                roomId = roomId,\n                senderId = currentUserId,\n                senderName = currentUserName,\n                content = entity.content,\n                messageType = type,\n                mediaUrl = mediaUrl,\n                timestamp = timestamp,\n                syncState = if (result.isSuccess) SyncState.SENT else SyncState.FAILED,\n                encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3\n            )',
    '                    messageType = type.name,\n                    mediaUrl = mediaUrl,\n                    timestamp = timestamp,\n                    encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3.name\n                )\n            )\n        }\n\n        if (result.isSuccess) {\n            messageDao.updateMessageSyncState(messageId, SyncState.SENT)\n        } else {\n            messageDao.updateMessageSyncState(messageId, SyncState.FAILED)\n        }\n\n        return Resource.Success(\n            Message(\n                id = messageId,\n                roomId = roomId,\n                senderId = currentUserId,\n                senderName = currentUserName,\n                content = "Attachment",\n                messageType = type,\n                mediaUrl = mediaUrl,\n                timestamp = timestamp,\n                syncState = if (result.isSuccess) SyncState.SENT else SyncState.FAILED,\n                encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3\n            )'
)

with open("app/src/main/java/com/hybrid/messaging/core/data/repository/MessageRepositoryImpl.kt", "w") as f:
    f.write(content)
