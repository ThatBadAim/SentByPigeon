package com.hybrid.messaging.feature.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hybrid.messaging.core.model.Message
import com.hybrid.messaging.feature.search.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onMessageClick: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val filters = listOf("All", "Media", "Links", "Documents", "Users", "Rooms")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                TextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onQueryChanged(it) },
                    placeholder = { Text("Search messages...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.performSearch(uiState.query)
                        }
                    ),
                    trailingIcon = {
                        if (uiState.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        ScrollableTabRow(
            selectedTabIndex = filters.indexOf(uiState.selectedFilter).coerceAtLeast(0),
            edgePadding = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            filters.forEach { filter ->
                Tab(
                    selected = uiState.selectedFilter == filter,
                    onClick = { viewModel.onFilterSelected(filter) },
                    text = { Text(filter) }
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.filteredResults.isEmpty() && uiState.query.isNotEmpty()) {
                    item {
                        Text("No results found for \"${uiState.query}\"", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(uiState.filteredResults) { message ->
                        SearchResultItem(
                            message = message,
                            searchQuery = uiState.query,
                            onClick = { onMessageClick(message.roomId, message.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    message: Message,
    searchQuery: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            val annotatedContent = highlightSearchQuery(message.content, searchQuery)
            Text(
                text = annotatedContent,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun highlightSearchQuery(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)

    val startIndex = text.indexOf(query, ignoreCase = true)
    if (startIndex == -1) return AnnotatedString(text)

    return buildAnnotatedString {
        append(text.substring(0, startIndex))
        withStyle(style = SpanStyle(background = Color.Yellow, color = Color.Black)) {
            append(text.substring(startIndex, startIndex + query.length))
        }
        append(text.substring(startIndex + query.length))
    }
}
