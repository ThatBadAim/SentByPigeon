package com.hybrid.messaging.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hybrid.messaging.core.database.entity.ChannelCategoryEntity
import com.hybrid.messaging.core.database.entity.ChatRoomEntity
import com.hybrid.messaging.core.database.entity.MessageEntity
import com.hybrid.messaging.core.database.entity.ReactionEntity
import com.hybrid.messaging.core.database.entity.ServerEntity
import com.hybrid.messaging.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
}

@Dao
interface ChatRoomDao {
    @Query("SELECT * FROM chat_rooms WHERE id = :roomId")
    fun getChatRoomById(roomId: String): Flow<ChatRoomEntity?>

    @Query("SELECT * FROM chat_rooms WHERE serverId IS NULL ORDER BY id DESC")
    fun getDirectAndGroupChats(): Flow<List<ChatRoomEntity>>

    @Query("SELECT * FROM chat_rooms WHERE serverId = :serverId ORDER BY name ASC")
    fun getChannelsForServer(serverId: String): Flow<List<ChatRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(chatRoom: ChatRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRooms(chatRooms: List<ChatRoomEntity>)
}

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers")
    fun getAllServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE id = :serverId")
    fun getServerById(serverId: String): Flow<ServerEntity?>

    @Query("SELECT * FROM channel_categories WHERE serverId = :serverId ORDER BY position ASC")
    fun getCategoriesForServer(serverId: String): Flow<List<ChannelCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<ChannelCategoryEntity>)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY timestamp DESC")
    fun getMessagesForRoom(roomId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMessageForRoom(roomId: String): Flow<MessageEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("""
        SELECT messages.* FROM messages
        JOIN messages_fts ON messages.id = messages_fts.rowid
        WHERE messages_fts MATCH :query
        ORDER BY messages.timestamp DESC
    """)
    suspend fun searchMessages(query: String): List<MessageEntity>
}

@Dao
interface ReactionDao {
    @Query("SELECT * FROM reactions WHERE messageId = :messageId")
    fun getReactionsForMessage(messageId: String): Flow<List<ReactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaction(reaction: ReactionEntity)

    @Query("DELETE FROM reactions WHERE messageId = :messageId AND emoji = :emoji AND userId = :userId")
    suspend fun removeReaction(messageId: String, emoji: String, userId: String)
}
