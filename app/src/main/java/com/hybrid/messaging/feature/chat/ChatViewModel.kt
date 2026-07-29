package com.hybrid.messaging.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hybrid.messaging.core.domain.repository.ChatRoomRepository
import com.hybrid.messaging.core.domain.repository.MessageRepository
import com.hybrid.messaging.core.domain.util.Resource
import com.hybrid.messaging.core.model.ChatRoom
import com.hybrid.messaging.core.model.Message
import com.hybrid.messaging.core.network.websocket.ConnectionState
import com.hybrid.messaging.core.network.websocket.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable

data class ChatUiState(
    val room: ChatRoom? = null,
    val messages: List<Message> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.CONNECTED,
    val isTyping: Boolean = false,
    val typingUserName: String? = null,
    val selectedMessageForReaction: Message? = null,
    val isRecordingVoiceNote: Boolean = false,
    val voiceNoteDurationMs: Long = 0L,
    val playingVoiceMessageId: String? = null,
    val isPlayingVoice: Boolean = false,
    val voicePlaybackProgress: Float = 0f,
    val unreadMessagesBelow: Int = 0,
    val inputQuery: String = ""
)

sealed class ChatUiEvent {
    data class UpdateInput(val text: String) : ChatUiEvent()
    data class SendMessage(val text: String) : ChatUiEvent()
    data class SendVoiceNote(val path: String, val durationMs: Long) : ChatUiEvent()
    data class AddReaction(val messageId: String, val emoji: String) : ChatUiEvent()
    data class SelectMessageForReaction(val message: Message?) : ChatUiEvent()
    data class ToggleVoicePlayback(val messageId: String) : ChatUiEvent()
    data object ToggleVoiceRecording : ChatUiEvent()
    data object ClearUnreadBadge : ChatUiEvent()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentRoomId: String = "general_channel"

    init {
        observeConnectionState()
        loadRoom("general_channel")
    }

    fun loadRoom(roomId: String) {
        currentRoomId = roomId
        viewModelScope.launch {
            chatRoomRepository.getChatRoom(roomId).collectLatest { room ->
                _uiState.update { it.copy(room = room) }
            }
        }

        viewModelScope.launch {
            messageRepository.getMessagesForRoom(roomId).collectLatest { messages ->
                _uiState.update { currentState ->
                    val updatedMessages = if (messages.isEmpty()) seedDefaultMessages(roomId) else messages
                    currentState.copy(messages = updatedMessages)
                }
            }
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            webSocketManager.connectionState.collectLatest { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
    }

    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.UpdateInput -> {
                _uiState.update { it.copy(inputQuery = event.text) }
            }
            is ChatUiEvent.SendMessage -> {
                if (event.text.isBlank()) return
                viewModelScope.launch {
                    val result = messageRepository.sendTextMessage(currentRoomId, event.text)
                    if (result is Resource.Success) {
                        _uiState.update { it.copy(inputQuery = "") }
                    }
                }
            }
            is ChatUiEvent.SendVoiceNote -> {
                viewModelScope.launch {
                    messageRepository.sendVoiceNote(currentRoomId, event.path, event.durationMs)
                }
            }
            is ChatUiEvent.AddReaction -> {
                viewModelScope.launch {
                    messageRepository.addReaction(event.messageId, event.emoji)
                    _uiState.update { it.copy(selectedMessageForReaction = null) }
                }
            }
            is ChatUiEvent.SelectMessageForReaction -> {
                _uiState.update { it.copy(selectedMessageForReaction = event.message) }
            }
            is ChatUiEvent.ToggleVoicePlayback -> {
                _uiState.update { currentState ->
                    if (currentState.playingVoiceMessageId == event.messageId) {
                        currentState.copy(isPlayingVoice = !currentState.isPlayingVoice)
                    } else {
                        currentState.copy(
                            playingVoiceMessageId = event.messageId,
                            isPlayingVoice = true,
                            voicePlaybackProgress = 0.35f
                        )
                    }
                }
            }
            is ChatUiEvent.ToggleVoiceRecording -> {
                _uiState.update { it.copy(isRecordingVoiceNote = !it.isRecordingVoiceNote) }
            }
            is ChatUiEvent.ClearUnreadBadge -> {
                _uiState.update { it.copy(unreadMessagesBelow = 0) }
            }
        }
    }

    private fun seedDefaultMessages(roomId: String): List<Message> {
        val now = System.currentTimeMillis()
        return listOf(
            Message(
                id = "msg_1",
                roomId = roomId,
                senderId = "user_me",
                senderName = "Alex Mercer",
                content = "Hey team! Architecture is fully set up with E2EE Signal Protocol hooks and Room SQLCipher encryption 🔐",
                timestamp = now - 600_000,
                reactions = listOf(
                    com.hybrid.messaging.core.model.Reaction("🚀", 3, listOf("user_1", "user_2", "user_me"), true),
                    com.hybrid.messaging.core.model.Reaction("🔥", 2, listOf("user_1", "user_2"), false)
                )
            ),
            Message(
                id = "msg_2",
                roomId = roomId,
                senderId = "user_sarah",
                senderName = "Sarah Connor",
                content = "Awesome! WebSockets & voice notes are super responsive. Discord channels + WhatsApp DMs in one app is legendary!",
                timestamp = now - 300_000
            ),
            Message(
                id = "msg_3",
                roomId = roomId,
                senderId = "user_sarah",
                senderName = "Sarah Connor",
                content = "Audio Note",
                messageType = com.hybrid.messaging.core.model.MessageType.VOICE_NOTE,
                mediaUrl = "sample_audio.mp3",
                audioDurationMs = 14000L,
                timestamp = now - 120_000
            )
        )
    }
}
