package com.hybrid.messaging.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hybrid.messaging.core.database.dao.ChatRoomDao
import com.hybrid.messaging.core.database.dao.MessageDao
import com.hybrid.messaging.core.database.dao.ServerDao
import com.hybrid.messaging.core.database.dao.UserDao
import com.hybrid.messaging.core.database.entity.ChannelCategoryEntity
import com.hybrid.messaging.core.database.entity.ChatRoomEntity
import com.hybrid.messaging.core.database.entity.MessageEntity
import com.hybrid.messaging.core.database.entity.ServerEntity
import com.hybrid.messaging.core.database.entity.UserEntity
import com.hybrid.messaging.core.model.ChatRoomType
import com.hybrid.messaging.core.model.EncryptionStatus
import com.hybrid.messaging.core.model.MessageType
import com.hybrid.messaging.core.model.UserStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var serverDao: ServerDao
    private lateinit var chatRoomDao: ChatRoomDao
    private lateinit var messageDao: MessageDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        userDao = db.userDao()
        serverDao = db.serverDao()
        chatRoomDao = db.chatRoomDao()
        messageDao = db.messageDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndFetchUser() = runTest {
        val user = UserEntity(
            id = "u1",
            phoneNumber = "+123",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = null,
            status = UserStatus.ONLINE,
            statusCustomMessage = null,
            publicKeyFingerprint = null,
            lastSeenTimestamp = 0L
        )

        userDao.insertUser(user)

        val fetchedUser = userDao.getUserById("u1").first()
        assertEquals(user, fetchedUser)
    }

    @Test
    fun updateUserPresenceStatus() = runTest {
        val user = UserEntity(
            id = "u1",
            phoneNumber = "+123",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = null,
            status = UserStatus.ONLINE,
            statusCustomMessage = null,
            publicKeyFingerprint = null,
            lastSeenTimestamp = 0L
        )
        userDao.insertUser(user)

        userDao.updateUserStatus("u1", UserStatus.AWAY)

        val updatedUser = userDao.getUserById("u1").first()
        assertEquals(UserStatus.AWAY, updatedUser?.status)
    }

    @Test
    fun getDirectAndGroupChats_QueriesOnlyNullServerId() = runTest {
        val dmRoom = ChatRoomEntity(
            id = "dm1", type = ChatRoomType.DIRECT_MESSAGE, name = "DM",
            description = null, avatarUrl = null, serverId = null, categoryId = null,
            isEncrypted = true, encryptionKeyId = "key", memberIds = listOf("u1", "u2")
        )
        val serverRoom = ChatRoomEntity(
            id = "ch1", type = ChatRoomType.SPACE_TEXT_CHANNEL, name = "Channel",
            description = null, avatarUrl = null, serverId = "s1", categoryId = "c1",
            isEncrypted = false, encryptionKeyId = null, memberIds = emptyList()
        )
        chatRoomDao.insertChatRooms(listOf(dmRoom, serverRoom))

        val dms = chatRoomDao.getDirectAndGroupChats().first()
        assertEquals(1, dms.size)
        assertEquals("dm1", dms[0].id)
    }

    @Test
    fun getPendingMessages_returnsMessagesWithPendingStatus() = runTest {
        val channel = ChatRoomEntity(
            id = "ch1", type = ChatRoomType.SPACE_TEXT_CHANNEL, name = "Channel 1",
            description = null, avatarUrl = null, serverId = null, categoryId = null,
            isEncrypted = false, encryptionKeyId = null, memberIds = emptyList()
        )
        chatRoomDao.insertChatRoom(channel)

        val pendingMessage = MessageEntity(
            id = "m1", roomId = "ch1", senderId = "u1", senderName = "User 1", senderAvatarUrl = null,
            content = "Pending", messageType = MessageType.TEXT, mediaUrl = null, audioDurationMs = null,
            timestamp = 1000L, encryptionStatus = EncryptionStatus.PENDING, replyToMessageId = null
        )
        val encryptedMessage = MessageEntity(
            id = "m2", roomId = "ch1", senderId = "u1", senderName = "User 1", senderAvatarUrl = null,
            content = "Encrypted", messageType = MessageType.TEXT, mediaUrl = null, audioDurationMs = null,
            timestamp = 1001L, encryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3, replyToMessageId = null
        )

        messageDao.insertMessages(listOf(pendingMessage, encryptedMessage))

        val pending = messageDao.getPendingMessages().first()
        assertEquals(1, pending.size)
        assertEquals("m1", pending[0].id)
    }

    @Test
    fun getMessagesForRoom_returnsOrderedByTimestampDesc() = runTest {
        val channel = ChatRoomEntity(
            id = "ch1", type = ChatRoomType.SPACE_TEXT_CHANNEL, name = "Channel 1",
            description = null, avatarUrl = null, serverId = null, categoryId = null,
            isEncrypted = false, encryptionKeyId = null, memberIds = emptyList()
        )
        chatRoomDao.insertChatRoom(channel)

        val msg1 = MessageEntity("m1", "ch1", "u1", "U", null, "1", MessageType.TEXT, null, null, 1000L, EncryptionStatus.UNENCRYPTED, null)
        val msg2 = MessageEntity("m2", "ch1", "u1", "U", null, "2", MessageType.TEXT, null, null, 2000L, EncryptionStatus.UNENCRYPTED, null)
        messageDao.insertMessages(listOf(msg1, msg2))

        val messages = messageDao.getMessagesForRoom("ch1").first()
        assertEquals(2, messages.size)
        assertEquals("m2", messages[0].id) // m2 is newer
        assertEquals("m1", messages[1].id)
    }

    @Test
    fun cascadingDelete_Server_CascadesToChannels() = runTest {
        val server = ServerEntity(id = "s1", name = "Test Server", iconUrl = null, ownerId = "u1")
        serverDao.insertServer(server)

        val category = ChannelCategoryEntity(id = "c1", serverId = "s1", name = "Category", position = 0)
        serverDao.insertCategories(listOf(category))

        val channel = ChatRoomEntity(
            id = "ch1",
            type = ChatRoomType.SPACE_TEXT_CHANNEL,
            name = "Channel 1",
            description = null,
            avatarUrl = null,
            serverId = "s1",
            categoryId = "c1",
            isEncrypted = false,
            encryptionKeyId = null,
            memberIds = emptyList()
        )
        chatRoomDao.insertChatRoom(channel)

        // Verify inserted
        assertEquals(1, chatRoomDao.getChannelsForServer("s1").first().size)

        // Delete server and verify cascading delete deletes the channels
        serverDao.deleteServer("s1")

        assertEquals(0, chatRoomDao.getChannelsForServer("s1").first().size)
    }

    @Test
    fun cascadingDelete_ChatRoom_CascadesToMessages() = runTest {
        val channel = ChatRoomEntity(
            id = "ch1",
            type = ChatRoomType.SPACE_TEXT_CHANNEL,
            name = "Channel 1",
            description = null,
            avatarUrl = null,
            serverId = null,
            categoryId = null,
            isEncrypted = false,
            encryptionKeyId = null,
            memberIds = emptyList()
        )
        chatRoomDao.insertChatRoom(channel)

        val message = MessageEntity(
            id = "m1",
            roomId = "ch1",
            senderId = "u1",
            senderName = "User 1",
            senderAvatarUrl = null,
            content = "Hello",
            messageType = MessageType.TEXT,
            mediaUrl = null,
            audioDurationMs = null,
            timestamp = 1000L,
            encryptionStatus = EncryptionStatus.UNENCRYPTED,
            replyToMessageId = null
        )
        messageDao.insertMessage(message)

        assertEquals(1, messageDao.getMessagesForRoom("ch1").first().size)

        chatRoomDao.deleteChatRoom("ch1")

        assertEquals(0, messageDao.getMessagesForRoom("ch1").first().size)
    }
}