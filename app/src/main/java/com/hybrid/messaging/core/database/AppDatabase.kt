package com.hybrid.messaging.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hybrid.messaging.core.database.dao.ChatRoomDao
import com.hybrid.messaging.core.database.dao.MessageDao
import com.hybrid.messaging.core.database.dao.ReactionDao
import com.hybrid.messaging.core.database.dao.ServerDao
import com.hybrid.messaging.core.database.dao.UserDao
import com.hybrid.messaging.core.database.entity.ChannelCategoryEntity
import com.hybrid.messaging.core.database.entity.ChatRoomEntity
import com.hybrid.messaging.core.database.entity.MessageEntity
import com.hybrid.messaging.core.database.entity.ReactionEntity
import com.hybrid.messaging.core.database.entity.ServerEntity
import com.hybrid.messaging.core.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ServerEntity::class,
        ChannelCategoryEntity::class,
        ChatRoomEntity::class,
        MessageEntity::class,
        ReactionEntity::class
    ],
    version = 2,
    autoMigrations = [
        androidx.room.AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun serverDao(): ServerDao
    abstract fun messageDao(): MessageDao
    abstract fun reactionDao(): ReactionDao

    companion object {
        const val DATABASE_NAME = "nexus_hybrid_messaging.db"
    }
}
