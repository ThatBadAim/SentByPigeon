package com.hybrid.messaging.core.database

import androidx.room.TypeConverter
import com.hybrid.messaging.core.model.ChatRoomType
import com.hybrid.messaging.core.model.EncryptionStatus
import com.hybrid.messaging.core.model.MessageType
import com.hybrid.messaging.core.model.UserStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromUserStatus(status: UserStatus): String = status.name

    @TypeConverter
    fun toUserStatus(value: String): UserStatus = runCatching { UserStatus.valueOf(value) }.getOrDefault(UserStatus.OFFLINE)

    @TypeConverter
    fun fromChatRoomType(type: ChatRoomType): String = type.name

    @TypeConverter
    fun toChatRoomType(value: String): ChatRoomType = runCatching { ChatRoomType.valueOf(value) }.getOrDefault(ChatRoomType.DIRECT_MESSAGE)

    @TypeConverter
    fun fromMessageType(type: MessageType): String = type.name

    @TypeConverter
    fun toMessageType(value: String): MessageType = runCatching { MessageType.valueOf(value) }.getOrDefault(MessageType.TEXT)

    @TypeConverter
    fun fromEncryptionStatus(status: EncryptionStatus): String = status.name

    @TypeConverter
    fun toEncryptionStatus(value: String): EncryptionStatus = runCatching { EncryptionStatus.valueOf(value) }.getOrDefault(EncryptionStatus.ENCRYPTED_SIGNAL_V3)

    @TypeConverter
    fun fromSyncState(status: com.hybrid.messaging.core.model.SyncState): String = status.name

    @TypeConverter
    fun toSyncState(value: String): com.hybrid.messaging.core.model.SyncState = runCatching { com.hybrid.messaging.core.model.SyncState.valueOf(value) }.getOrDefault(com.hybrid.messaging.core.model.SyncState.PENDING)

    @TypeConverter
    fun fromListString(list: List<String>): String = json.encodeToString(list)

    @TypeConverter
    fun toListString(value: String): List<String> = runCatching { json.decodeFromString<List<String>>(value) }.getOrDefault(emptyList())
}
