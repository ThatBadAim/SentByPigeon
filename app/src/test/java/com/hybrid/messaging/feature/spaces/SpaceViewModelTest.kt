package com.hybrid.messaging.feature.spaces

import app.cash.turbine.test
import com.hybrid.messaging.core.domain.repository.ServerRepository
import com.hybrid.messaging.core.model.ChatRoom
import com.hybrid.messaging.core.model.ChatRoomType
import com.hybrid.messaging.core.model.Server
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class SpaceViewModelTest {

    private lateinit var viewModel: SpaceViewModel
    private val serverRepository: ServerRepository = mockk()

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock default flows
        coEvery { serverRepository.getServers() } returns flowOf(emptyList())
        coEvery { serverRepository.getServerDetails(any()) } returns flowOf(null)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SelectServer event updates selected server`() = runTest {
        val testServer = Server(id = "srv_1", name = "Test Server", ownerId = "owner")
        coEvery { serverRepository.getServerDetails("srv_1") } returns flowOf(testServer)

        viewModel = SpaceViewModel(serverRepository)

        viewModel.uiState.test {
            // Wait for initial init flow emission
            var state = awaitItem()

            viewModel.onEvent(SpaceUiEvent.SelectServer("srv_1"))

            // Expected updates: selectedServerId -> then selectedServer
            state = awaitItem()
            while(state.selectedServerId != "srv_1" || state.selectedServer != testServer) {
                 state = awaitItem()
            }

            assertEquals("srv_1", state.selectedServerId)
            assertEquals(testServer, state.selectedServer)
        }
    }

    @Test
    fun `SelectChannel event updates selected channel for text channels`() = runTest {
        viewModel = SpaceViewModel(serverRepository)

        val textChannel = ChatRoom(id = "ch_1", type = ChatRoomType.SPACE_TEXT_CHANNEL, name = "General")

        viewModel.onEvent(SpaceUiEvent.SelectChannel(textChannel))

        assertEquals("ch_1", viewModel.uiState.value.selectedChannelId)
    }

    @Test
    fun `SelectChannel event triggers connect for voice channels`() = runTest {
        viewModel = SpaceViewModel(serverRepository)

        val voiceChannel = ChatRoom(id = "vc_1", type = ChatRoomType.SPACE_VOICE_ROOM, name = "Lounge")

        viewModel.onEvent(SpaceUiEvent.SelectChannel(voiceChannel))

        assertEquals(voiceChannel, viewModel.uiState.value.connectedVoiceChannel)
    }

    @Test
    fun `ToggleMic event toggles mic state`() = runTest {
        viewModel = SpaceViewModel(serverRepository)

        val initialState = viewModel.uiState.value.isMicMuted

        viewModel.onEvent(SpaceUiEvent.ToggleMic)

        assertEquals(!initialState, viewModel.uiState.value.isMicMuted)
    }
}