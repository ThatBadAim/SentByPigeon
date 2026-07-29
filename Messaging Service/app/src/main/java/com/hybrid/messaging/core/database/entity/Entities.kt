package com.hybrid.messaging.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hybrid.messaging.core.model.ChatRoomType
import com.hybrid.messaging.core.model.EncryptionStatus
import com.hybrid.messaging.core.model.MessageType
import com.hybrid.messaging.core.model.RolePermission
import com.hybrid.messaging.core.model.UserStatus

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val phoneNumber: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val status: UserStatus,
    val statusCustomMessage: String?,
    val publicKeyFingerprint: String?,
    val lastSeenTimestamp: Long
)

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconUrl: String?,
    val ownerId: String
)

@Entity(
    tableName = "channel_categories",
    foreignKeys = [
        ForeignKey(
            entity = ServerEntity::class,
            parentColumns = ["id"],
            childColumns = ["serverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("serverId")]
)
data class ChannelCategoryEntity(
    @PrimaryKey val id: String,
    val serverId: String,
    val name: String,
    val position: Int
)

@Entity(
    tableName = "chat_rooms",
    indices = [Index("serverId"), Index("categoryId")]
)
data class ChatRoomEntity(
    @PrimaryKey val id: String,
    val type: ChatRoomType,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val serverId: String?,
    val categoryId: String?,
    val isEncrypted: Boolean,
    val encryptionKeyId: String?,
    val memberIds: List<String>
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roomId"), Index("senderId"), Index("timestamp")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatarUrl: String?,
    val content: String,
    val messageType: MessageType,
    val mediaUrl: String?,
    val audioDurationMs: Long?,
    val timestamp: Long,
    val encryptionStatus: EncryptionStatus,
    val replyToMessageId: String?
)

@Entity(
    tableName = "reactions",
    primaryKeys = ["messageId", "emoji", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("messageId")]
)
data class ReactionEntity(
    val messageId: String,
    val emoji: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "roles",
    foreignKeys = [
        ForeignKey(
            entity = ServerEntity::class,
            parentColumns = ["id"],
            childColumns = ["serverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("serverId")]
)
data class RoleEntity(
    @PrimaryKey val id: String,
    val serverId: String,
    val name: String,
    val colorHex: String,
    val permissions: Set<RolePermission>
)

@Entity(
    tableName = "server_members",
    primaryKeys = ["serverId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = ServerEntity::class,
            parentColumns = ["id"],
            childColumns = ["serverId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("serverId"), Index("userId")]
)
data class ServerMemberEntity(
    val serverId: String,
    val userId: String,
    val roleIds: List<String>
)

@Entity(
    tableName = "channel_permission_overrides",
    primaryKeys = ["channelId", "roleId"],
    foreignKeys = [
        ForeignKey(
            entity = ChatRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["channelId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoleEntity::class,
            parentColumns = ["id"],
            childColumns = ["roleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("channelId"), Index("roleId")]
)
data class ChannelPermissionOverrideEntity(
    val channelId: String,
    val roleId: String,
    val allowedPermissions: Set<RolePermission>,
    val deniedPermissions: Set<RolePermission>
)
