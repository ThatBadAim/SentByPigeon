import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/core/data/repository/MessageRepositoryImpl.kt"
with open(filepath, "r") as f:
    content = f.read()

search_method = """    override suspend fun searchMessages(query: String): Resource<List<Message>> {
        return try {
            val ftsQuery = "*$query*"
            val entities = messageDao.searchMessages(ftsQuery)
            val domainMessages = entities.map { entity ->
                Message(
                    id = entity.id,
                    roomId = entity.roomId,
                    senderId = entity.senderId,
                    senderName = entity.senderName,
                    senderAvatarUrl = entity.senderAvatarUrl,
                    content = entity.content,
                    messageType = entity.messageType,
                    mediaUrl = entity.mediaUrl,
                    audioDurationMs = entity.audioDurationMs,
                    timestamp = entity.timestamp,
                    encryptionStatus = entity.encryptionStatus,
                    reactions = emptyList(),
                    readReceipts = emptyList(),
                    replyToMessageId = entity.replyToMessageId
                )
            }
            Resource.Success(domainMessages)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to search messages")
        }
    }
}"""

content = content.replace("""    override suspend fun searchMessages(query: String): Resource<List<Message>> {
        return try {
            val ftsQuery = "*$query*"
            val entities = messageDao.searchMessages(ftsQuery)
            Resource.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to search messages")
        }
    }
}""", search_method)

with open(filepath, "w") as f:
    f.write(content)
