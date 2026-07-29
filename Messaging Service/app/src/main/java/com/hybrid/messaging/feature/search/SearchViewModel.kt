package com.hybrid.messaging.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hybrid.messaging.core.domain.repository.MessageRepository
import com.hybrid.messaging.core.domain.util.Resource
import com.hybrid.messaging.core.model.Message
import com.hybrid.messaging.core.model.MessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Message> = emptyList(),
    val filteredResults: List<Message> = emptyList(),
    val selectedFilter: String = "All",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        performSearch(query)
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilter()
    }

    fun performSearch(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), filteredResults = emptyList()) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = messageRepository.searchMessages(query)
            if (result is Resource.Success) {
                _uiState.update {
                    it.copy(results = result.data ?: emptyList(), isLoading = false, error = null)
                }
                applyFilter()
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    private fun applyFilter() {
        _uiState.update { state ->
            val filtered = when (state.selectedFilter) {
                "Media" -> state.results.filter { it.messageType == MessageType.IMAGE || it.mediaUrl != null }
                "Links" -> state.results.filter { it.content.contains("http") }
                "Documents" -> state.results.filter { it.messageType == MessageType.SYSTEM } // Simple placeholder
                else -> state.results
            }
            state.copy(filteredResults = filtered)
        }
    }
}
