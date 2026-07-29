package com.hybrid.messaging.core.data.repository

import com.hybrid.messaging.core.database.dao.ChatRoomDao
import com.hybrid.messaging.core.database.entity.ChatRoomEntity
import com.hybrid.messaging.core.domain.repository.ChatRoomRepository
import com.hybrid.messaging.core.domain.util.Resource
import com.hybrid.messaging.core.model.ChatRoom
import com.hybrid.messaging.core.model.ChatRoomType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ChatRoomRepositoryImpl @Inject constructor(
    private val chatRoomDao: ChatRoomDao
) : ChatRoomRepository {

    override fun getDirectAndGroupChats(): Flow<List<ChatRoom>> {
        return chatRoomDao.getDirectAndGroupChats().map { entities ->
            entities.map { entity ->
                ChatRoom(
                    id = entity.id,
                    type = entity.type,
                    name = entity.name,
                    description = entity.description,
                    avatarUrl = entity.avatarUrl,
                    serverId = null,
                    categoryId = null,
                    isEncrypted = entity.isEncrypted,
                    encryptionKeyId = entity.encryptionKeyId,
                    memberIds = entity.memberIds
                )
            }
        }
    }

    override fun getChatRoom(roomId: String): Flow<ChatRoom?> {
        return chatRoomDao.getChatRoomById(roomId).map { entity ->
            entity?.let {
                ChatRoom(
                    id = it.id,
                    type = it.type,
                    name = it.name,
                    description = it.description,
                    avatarUrl = it.avatarUrl,
                    serverId = it.serverId,
                    categoryId = it.categoryId,
                    isEncrypted = it.isEncrypted,
                    encryptionKeyId = it.encryptionKeyId,
                    memberIds = it.memberIds
                )
            }
        }
    }

    override suspend fun createDirectMessage(targetUserId: String): Resource<ChatRoom> {
        val roomId = UUID.randomUUID().toString()
        val entity = ChatRoomEntity(
            id = roomId,
            type = ChatRoomType.DIRECT_MESSAGE,
            name = "Direct Message",
            description = null,
            avatarUrl = null,
            serverId = null,
            categoryId = null,
            isEncrypted = true,
            encryptionKeyId = "sig_v3_${UUID.randomUUID()}",
            memberIds = listOf("user_me", targetUserId)
        )
        chatRoomDao.insertChatRoom(entity)

        return Resource.Success(
            ChatRoom(
                id = roomId,
                type = ChatRoomType.DIRECT_MESSAGE,
                name = entity.name,
                isEncrypted = true,
                memberIds = entity.memberIds
            )
        )
    }

    override suspend fun createGroupChat(name: String, memberIds: List<String>): Resource<ChatRoom> {
        val roomId = UUID.randomUUID().toString()
        val allMembers = memberIds + "user_me"
        val entity = ChatRoomEntity(
            id = roomId,
            type = ChatRoomType.GROUP_CHAT,
            name = name,
            description = "E2EE Group Chat",
            avatarUrl = null,
            serverId = null,
            categoryId = null,
            isEncrypted = true,
            encryptionKeyId = "sig_v3_group_${UUID.randomUUID()}",
            memberIds = allMembers
        )
        chatRoomDao.insertChatRoom(entity)

        return Resource.Success(
            ChatRoom(
                id = roomId,
                type = ChatRoomType.GROUP_CHAT,
                name = name,
                isEncrypted = true,
                memberIds = allMembers
            )
        )
    }
}
