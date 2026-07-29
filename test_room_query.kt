package com.hybrid.messaging.core.database.dao
import androidx.room.*
@Dao
interface SearchDao {
    @Query("""
        SELECT * FROM messages
        JOIN messages_fts ON messages.id = messages_fts.rowid
        WHERE messages_fts MATCH :query
    """)
    fun searchMessages(query: String): List<com.hybrid.messaging.core.database.entity.MessageEntity>
}
