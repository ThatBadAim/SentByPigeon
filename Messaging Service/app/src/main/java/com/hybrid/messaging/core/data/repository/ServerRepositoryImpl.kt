package com.hybrid.messaging.core.data.repository

import com.hybrid.messaging.core.database.dao.ChatRoomDao
import com.hybrid.messaging.core.database.dao.ServerDao
import com.hybrid.messaging.core.database.entity.ChannelCategoryEntity
import com.hybrid.messaging.core.database.entity.ChatRoomEntity
import com.hybrid.messaging.core.database.entity.ServerEntity
import com.hybrid.messaging.core.domain.repository.ServerRepository
import com.hybrid.messaging.core.domain.util.Resource
import com.hybrid.messaging.core.model.ChannelCategory
import com.hybrid.messaging.core.model.ChatRoom
import com.hybrid.messaging.core.model.ChatRoomType
import com.hybrid.messaging.core.model.Server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ServerRepositoryImpl @Inject constructor(
    private val serverDao: ServerDao,
    private val chatRoomDao: ChatRoomDao
) : ServerRepository {

    override fun getServers(): Flow<List<Server>> {
        return serverDao.getAllServers().map { entities ->
            entities.map { entity ->
                Server(
                    id = entity.id,
                    name = entity.name,
                    iconUrl = entity.iconUrl,
                    ownerId = entity.ownerId
                )
            }
        }
    }

    override fun getServerDetails(serverId: String): Flow<Server?> {
        val serverFlow = serverDao.getServerById(serverId)
        val categoriesFlow = serverDao.getCategoriesForServer(serverId)
        val channelsFlow = chatRoomDao.getChannelsForServer(serverId)

        return combine(serverFlow, categoriesFlow, channelsFlow) { serverEntity, categoryEntities, channelEntities ->
            if (serverEntity == null) return@combine null

            val domainChannels = channelEntities.map { entity ->
                ChatRoom(
                    id = entity.id,
                    type = entity.type,
                    name = entity.name,
                    description = entity.description,
                    avatarUrl = entity.avatarUrl,
                    serverId = entity.serverId,
                    categoryId = entity.categoryId,
                    isEncrypted = entity.isEncrypted,
                    encryptionKeyId = entity.encryptionKeyId,
                    memberIds = entity.memberIds
                )
            }

            val domainCategories = categoryEntities.map { catEntity ->
                ChannelCategory(
                    id = catEntity.id,
                    serverId = catEntity.serverId,
                    name = catEntity.name,
                    position = catEntity.position,
                    channels = domainChannels.filter { it.categoryId == catEntity.id }
                )
            }

            Server(
                id = serverEntity.id,
                name = serverEntity.name,
                iconUrl = serverEntity.iconUrl,
                ownerId = serverEntity.ownerId,
                categories = domainCategories
            )
        }
    }

    override suspend fun createServer(name: String, iconUrl: String?): Resource<Server> {
        val serverId = UUID.randomUUID().toString()
        val ownerId = "user_me"

        val serverEntity = ServerEntity(
            id = serverId,
            name = name,
            iconUrl = iconUrl,
            ownerId = ownerId
        )
        serverDao.insertServer(serverEntity)

        // Seed default categories and general channel
        val textCatId = UUID.randomUUID().toString()
        val voiceCatId = UUID.randomUUID().toString()

        serverDao.insertCategories(
            listOf(
                ChannelCategoryEntity(id = textCatId, serverId = serverId, name = "TEXT CHANNELS", position = 0),
                ChannelCategoryEntity(id = voiceCatId, serverId = serverId, name = "VOICE CHANNELS", position = 1)
            )
        )

        val generalChannel = ChatRoomEntity(
            id = UUID.randomUUID().toString(),
            type = ChatRoomType.SPACE_TEXT_CHANNEL,
            name = "general",
            description = "General discussion channel",
            avatarUrl = null,
            serverId = serverId,
            categoryId = textCatId,
            isEncrypted = false,
            encryptionKeyId = null,
            memberIds = listOf(ownerId)
        )
        chatRoomDao.insertChatRoom(generalChannel)

        return Resource.Success(
            Server(
                id = serverId,
                name = name,
                iconUrl = iconUrl,
                ownerId = ownerId
            )
        )
    }

    override suspend fun createCategory(serverId: String, name: String): Resource<Unit> {
        val categoryEntity = ChannelCategoryEntity(
            id = UUID.randomUUID().toString(),
            serverId = serverId,
            name = name.uppercase(),
            position = 99
        )
        serverDao.insertCategories(listOf(categoryEntity))
        return Resource.Success(Unit)
    }

    override suspend fun createChannel(
        serverId: String,
        categoryId: String?,
        name: String,
        isVoice: Boolean
    ): Resource<ChatRoom> {
        val channelId = UUID.randomUUID().toString()
        val type = if (isVoice) ChatRoomType.SPACE_VOICE_ROOM else ChatRoomType.SPACE_TEXT_CHANNEL

        val entity = ChatRoomEntity(
            id = channelId,
            type = type,
            name = name.lowercase().replace(" ", "-"),
            description = null,
            avatarUrl = null,
            serverId = serverId,
            categoryId = categoryId,
            isEncrypted = false,
            encryptionKeyId = null,
            memberIds = listOf("user_me")
        )
        chatRoomDao.insertChatRoom(entity)

        return Resource.Success(
            ChatRoom(
                id = channelId,
                type = type,
                name = entity.name,
                serverId = serverId,
                categoryId = categoryId
            )
        )
    }
}
