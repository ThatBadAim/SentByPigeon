import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/core/data/repository/MessageRepositoryImpl.kt"
with open(filepath, "r") as f:
    content = f.read()

search_method = """    override suspend fun markRoomAsRead(roomId: String): Resource<Unit> {
        // Optimistic local update
        return Resource.Success(Unit)
    }

    override suspend fun searchMessages(query: String): Resource<List<Message>> {
        return try {
            val ftsQuery = "*$query*"
            val entities = messageDao.searchMessages(ftsQuery)
            Resource.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to search messages")
        }
    }
}"""

content = content.replace("""    override suspend fun markRoomAsRead(roomId: String): Resource<Unit> {
        // Optimistic local update
        return Resource.Success(Unit)
    }
}""", search_method)

with open(filepath, "w") as f:
    f.write(content)
