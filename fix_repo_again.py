import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/core/domain/repository/Repositories.kt"
with open(filepath, "r") as f:
    content = f.read()

content = content.replace("suspend fun markRoomAsRead(roomId: String): Resource<Unit>", "suspend fun markRoomAsRead(roomId: String): Resource<Unit>\n    suspend fun searchMessages(query: String): Resource<List<Message>>")

with open(filepath, "w") as f:
    f.write(content)
