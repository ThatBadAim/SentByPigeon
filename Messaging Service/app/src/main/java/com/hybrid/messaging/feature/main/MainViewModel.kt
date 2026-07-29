package com.hybrid.messaging.feature.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class NavigationTab {
    CHATS,
    SPACES,
    SEARCH,
    CALLS,
    SETTINGS
}

data class MainUiState(
    val currentTab: NavigationTab = NavigationTab.SPACES,
    val activeChatRoomId: String? = null,
    val focusedMessageId: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun selectTab(tab: NavigationTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun openChatRoom(roomId: String, messageId: String? = null) {
        _uiState.value = _uiState.value.copy(
            currentTab = NavigationTab.CHATS,
            activeChatRoomId = roomId,
            focusedMessageId = messageId
        )
    }
}
