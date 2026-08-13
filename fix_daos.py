import re

with open("app/src/main/java/com/hybrid/messaging/core/database/dao/Daos.kt", "r") as f:
    content = f.read()

content = content.replace(
    'import com.hybrid.messaging.core.database.entity.UserEntity\nimport kotlinx.coroutines.flow.Flow\n',
    'import com.hybrid.messaging.core.database.entity.UserEntity\nimport com.hybrid.messaging.core.model.SyncState\nimport kotlinx.coroutines.flow.Flow\n'
)

content = content.replace(
    '@Query("SELECT * FROM messages WHERE encryptionStatus = \'PENDING\' ORDER BY timestamp ASC")\n    fun getPendingMessages(): Flow<List<MessageEntity>>\n',
    '@Query("SELECT * FROM messages WHERE syncState IN (\'PENDING\', \'FAILED\') ORDER BY timestamp ASC")\n    fun getPendingMessages(): Flow<List<MessageEntity>>\n\n    @Query("UPDATE messages SET syncState = :syncState WHERE id = :messageId")\n    suspend fun updateMessageSyncState(messageId: String, syncState: SyncState)\n'
)

with open("app/src/main/java/com/hybrid/messaging/core/database/dao/Daos.kt", "w") as f:
    f.write(content)
