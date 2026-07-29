package com.hybrid.messaging.feature.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hybrid.messaging.core.domain.repository.ServerRepository
import com.hybrid.messaging.core.model.ChannelCategory
import com.hybrid.messaging.core.model.ChatRoom
import com.hybrid.messaging.core.model.ChatRoomType
import com.hybrid.messaging.core.model.Server
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpaceUiState(
    val servers: List<Server> = emptyList(),
    val selectedServerId: String? = null,
    val selectedServer: Server? = null,
    val selectedChannelId: String? = null,
    val connectedVoiceChannel: ChatRoom? = null,
    val isMicMuted: Boolean = false,
    val isDeafened: Boolean = false
)

sealed class SpaceUiEvent {
    data class SelectServer(val serverId: String) : SpaceUiEvent()
    data class SelectChannel(val channel: ChatRoom) : SpaceUiEvent()
    data class ConnectVoiceChannel(val channel: ChatRoom) : SpaceUiEvent()
    data object DisconnectVoice : SpaceUiEvent()
    data object ToggleMic : SpaceUiEvent()
    data object ToggleDeafen : SpaceUiEvent()
    data class CreateServer(val name: String) : SpaceUiEvent()
}

@HiltViewModel
class SpaceViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpaceUiState())
    val uiState: StateFlow<SpaceUiState> = _uiState.asStateFlow()

    init {
        observeServers()
    }

    private fun observeServers() {
        viewModelScope.launch {
            serverRepository.getServers().collectLatest { servers ->
                val finalServers = if (servers.isEmpty()) seedDefaultServers() else servers
                _uiState.update { currentState ->
                    val firstServer = finalServers.firstOrNull()
                    currentState.copy(
                        servers = finalServers,
                        selectedServerId = currentState.selectedServerId ?: firstServer?.id
                    )
                }

                _uiState.value.selectedServerId?.let { serverId ->
                    observeServerDetails(serverId)
                }
            }
        }
    }

    private fun observeServerDetails(serverId: String) {
        viewModelScope.launch {
            serverRepository.getServerDetails(serverId).collectLatest { server ->
                _uiState.update { currentState ->
                    val defaultServer = server ?: seedDefaultServerDetails(serverId)
                    val defaultChannel = defaultServer.categories.flatMap { it.channels }.firstOrNull()
                    currentState.copy(
                        selectedServer = defaultServer,
                        selectedChannelId = currentState.selectedChannelId ?: defaultChannel?.id
                    )
                }
            }
        }
    }

    fun onEvent(event: SpaceUiEvent) {
        when (event) {
            is SpaceUiEvent.SelectServer -> {
                _uiState.update { it.copy(selectedServerId = event.serverId) }
                observeServerDetails(event.serverId)
            }
            is SpaceUiEvent.SelectChannel -> {
                if (event.channel.type == ChatRoomType.SPACE_VOICE_ROOM) {
                    onEvent(SpaceUiEvent.ConnectVoiceChannel(event.channel))
                } else {
                    _uiState.update { it.copy(selectedChannelId = event.channel.id) }
                }
            }
            is SpaceUiEvent.ConnectVoiceChannel -> {
                _uiState.update { it.copy(connectedVoiceChannel = event.channel) }
            }
            is SpaceUiEvent.DisconnectVoice -> {
                _uiState.update { it.copy(connectedVoiceChannel = null) }
            }
            is SpaceUiEvent.ToggleMic -> {
                _uiState.update { it.copy(isMicMuted = !it.isMicMuted) }
            }
            is SpaceUiEvent.ToggleDeafen -> {
                _uiState.update { it.copy(isDeafened = !it.isDeafened) }
            }
            is SpaceUiEvent.CreateServer -> {
                viewModelScope.launch {
                    serverRepository.createServer(event.name, null)
                }
            }
        }
    }

    private fun seedDefaultServers(): List<Server> {
        return listOf(
            Server("srv_1", "Kotlin Architects", null, "user_me"),
            Server("srv_2", "Crypto & Web3 E2EE", null, "user_me"),
            Server("srv_3", "Gaming Lounge", null, "user_me")
        )
    }

    private fun seedDefaultServerDetails(serverId: String): Server {
        val cat1 = ChannelCategory(
            id = "cat_1",
            serverId = serverId,
            name = "INFORMATION",
            position = 0,
            channels = listOf(
                ChatRoom("ch_welcome", ChatRoomType.SPACE_TEXT_CHANNEL, "welcome-rules", serverId = serverId, categoryId = "cat_1"),
                ChatRoom("ch_announcements", ChatRoomType.SPACE_TEXT_CHANNEL, "announcements", serverId = serverId, categoryId = "cat_1")
            )
        )

        val cat2 = ChannelCategory(
            id = "cat_2",
            serverId = serverId,
            name = "TEXT CHANNELS",
            position = 1,
            channels = listOf(
                ChatRoom("general_channel", ChatRoomType.SPACE_TEXT_CHANNEL, "general", serverId = serverId, categoryId = "cat_2"),
                ChatRoom("ch_architecture", ChatRoomType.SPACE_TEXT_CHANNEL, "architecture-design", serverId = serverId, categoryId = "cat_2")
            )
        )

        val cat3 = ChannelCategory(
            id = "cat_3",
            serverId = serverId,
            name = "VOICE ROOMS",
            position = 2,
            channels = listOf(
                ChatRoom("v_lounge", ChatRoomType.SPACE_VOICE_ROOM, "🔊 General Lounge", serverId = serverId, categoryId = "cat_3"),
                ChatRoom("v_pair_prog", ChatRoomType.SPACE_VOICE_ROOM, "🔊 Pair Programming", serverId = serverId, categoryId = "cat_3")
            )
        )

        return Server(
            id = serverId,
            name = if (serverId == "srv_1") "Kotlin Architects" else "Community Space",
            ownerId = "user_me",
            categories = listOf(cat1, cat2, cat3)
        )
    }
}
