package com.hybrid.messaging.feature.chat

import app.cash.turbine.test
import com.hybrid.messaging.core.domain.repository.ChatRoomRepository
import com.hybrid.messaging.core.domain.repository.MessageRepository
import com.hybrid.messaging.core.domain.util.Resource
import com.hybrid.messaging.core.model.ChatRoom
import com.hybrid.messaging.core.model.ChatRoomType
import com.hybrid.messaging.core.model.Message
import com.hybrid.messaging.core.network.websocket.ConnectionState
import com.hybrid.messaging.core.network.websocket.WebSocketManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private lateinit var viewModel: ChatViewModel
    private val messageRepository: MessageRepository = mockk()
    private val chatRoomRepository: ChatRoomRepository = mockk()
    private val webSocketManager: WebSocketManager = mockk()

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock default flows required for initialization
        val mockConnectionState = MutableStateFlow(ConnectionState.CONNECTED)
        every { webSocketManager.connectionState } returns mockConnectionState

        coEvery { chatRoomRepository.getChatRoom("general_channel") } returns flowOf(null)
        coEvery { messageRepository.getMessagesForRoom("general_channel") } returns flowOf(emptyList())

        viewModel = ChatViewModel(messageRepository, chatRoomRepository, webSocketManager)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadRoom updates room and messages state`() = runTest {
        val testRoom = ChatRoom(id = "room1", type = ChatRoomType.DIRECT_MESSAGE, name = "Test Room")
        val testMessage = Message(id = "msg1", roomId = "room1", senderId = "user1", senderName = "User 1", content = "Hello", timestamp = 0L)

        coEvery { chatRoomRepository.getChatRoom("room1") } returns flowOf(testRoom)
        coEvery { messageRepository.getMessagesForRoom("room1") } returns flowOf(listOf(testMessage))

        viewModel.loadRoom("room1")

        viewModel.uiState.test {
            // Skip initial state from init
            val state = awaitItem()

            // It could be the state with just room updated, or just messages updated, or both depending on coroutine scheduling.
            // Wait for both to be populated.
            var finalState = state
            while(finalState.room != testRoom || finalState.messages != listOf(testMessage)) {
                finalState = awaitItem()
            }

            assertEquals(testRoom, finalState.room)
            assertEquals(listOf(testMessage), finalState.messages)
        }
    }

    @Test
    fun `UpdateInput event updates input query`() = runTest {
        viewModel.onEvent(ChatUiEvent.UpdateInput("Hello Test"))

        assertEquals("Hello Test", viewModel.uiState.value.inputQuery)
    }

    @Test
    fun `SendMessage event clears input query on success`() = runTest {
        // Setup initial input query
        viewModel.onEvent(ChatUiEvent.UpdateInput("Hello Send"))

        val testMessage = Message(id = "msg_sent", roomId = "general_channel", senderId = "user1", senderName = "User", content = "Hello Send", timestamp = 1L)
        coEvery { messageRepository.sendTextMessage("general_channel", "Hello Send") } returns Resource.Success(testMessage)

        viewModel.onEvent(ChatUiEvent.SendMessage("Hello Send"))

        // Advance dispatcher to execute coroutine
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.inputQuery)
        coVerify { messageRepository.sendTextMessage("general_channel", "Hello Send") }
    }
}