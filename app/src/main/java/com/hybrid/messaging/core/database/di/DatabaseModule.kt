package com.hybrid.messaging.core.database.di

import android.content.Context
import android.util.Base64
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hybrid.messaging.core.database.AppDatabase
import com.hybrid.messaging.core.database.dao.ChatRoomDao
import com.hybrid.messaging.core.database.dao.MessageDao
import com.hybrid.messaging.core.database.dao.ReactionDao
import com.hybrid.messaging.core.database.dao.ServerDao
import com.hybrid.messaging.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSupportFactory(@ApplicationContext context: Context): SupportFactory {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            "db_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var dbKeyBase64 = prefs.getString("db_key", null)
        if (dbKeyBase64 == null) {
            val randomKey = ByteArray(32)
            SecureRandom().nextBytes(randomKey)
            dbKeyBase64 = Base64.encodeToString(randomKey, Base64.NO_WRAP)
            prefs.edit().putString("db_key", dbKeyBase64).apply()
        }

        val passphrase = SQLiteDatabase.getBytes(dbKeyBase64!!.toCharArray())
        return SupportFactory(passphrase)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        factory: SupportFactory
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .openHelperFactory(factory)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideChatRoomDao(db: AppDatabase): ChatRoomDao = db.chatRoomDao()

    @Provides
    fun provideServerDao(db: AppDatabase): ServerDao = db.serverDao()

    @Provides
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideReactionDao(db: AppDatabase): ReactionDao = db.reactionDao()
}
