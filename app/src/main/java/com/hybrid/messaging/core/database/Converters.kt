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
    fromUserStatus(status: UserStatus): String = status.name

    @TypeConverter
    toUserStatus(value: String): UserStatus = runCatching { UserStatus.valueOf(value) }.getOrDefault(UserStatus.OFFLINE)

    @TypeConverter
    fromChatRoomType(type: ChatRoomType): String = type.name

    @TypeConverter
    toChatRoomType(value: String): ChatRoomType = runCatching { ChatRoomType.valueOf(value) }.getOrDefault(ChatRoomType.DIRECT_MESSAGE)

    @TypeConverter
    fromMessageType(type: MessageType): String = type.name

    @TypeConverter
    toMessageType(value: String): MessageType = runCatching { MessageType.valueOf(value) }.getOrDefault(MessageType.TEXT)

    @TypeConverter
    fromEncryptionStatus(status: EncryptionStatus): String = status.name

    @TypeConverter
    toEncryptionStatus(value: String): EncryptionStatus = runCatching { EncryptionStatus.valueOf(value) }.getOrDefault(EncryptionStatus.ENCRYPTED_SIGNAL_V3)

    @TypeConverter
    fromListString(list: List<String>): String = json.encodeToString(list)

    @TypeConverter
    toListString(value: String): List<String> = runCatching { json.decodeFromString<List<String>>(value) }.getOrDefault(emptyList())
}
