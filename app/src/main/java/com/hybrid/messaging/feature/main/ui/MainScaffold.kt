package com.hybrid.messaging.feature.main.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.hybrid.messaging.feature.chat.ChatViewModel
import com.hybrid.messaging.feature.chat.ui.ChatScreen
import com.hybrid.messaging.feature.main.MainUiState
import com.hybrid.messaging.feature.main.MainViewModel
import com.hybrid.messaging.feature.main.NavigationTab
import com.hybrid.messaging.feature.spaces.SpaceViewModel
import com.hybrid.messaging.feature.spaces.ui.SpaceNavigationDrawer

@Composable
fun MainScaffold(
    mainViewModel: MainViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    spaceViewModel: SpaceViewModel = hiltViewModel()
) {
    val uiState by mainViewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            HybridBottomNavigationBar(
                currentTab = uiState.currentTab,
                onTabSelected = { mainViewModel.selectTab(it) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.currentTab) {
                NavigationTab.CHATS -> {
                    ChatScreen(
                        viewModel = chatViewModel,
                        onNavigateBack = { mainViewModel.selectTab(NavigationTab.SPACES) }
                    )
                }
                NavigationTab.SPACES -> {
                    SpaceNavigationDrawer(
                        viewModel = spaceViewModel,
                        onChannelClick = { channel ->
                            chatViewModel.loadRoom(channel.id)
                            mainViewModel.selectTab(NavigationTab.CHATS)
                        }
                    )
                }
                NavigationTab.CALLS -> {
                    CallsTabScreen()
                }
                NavigationTab.SETTINGS -> {
                    SettingsTabScreen()
                }
            }
        }
    }
}

@Composable
fun HybridBottomNavigationBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            selected = currentTab == NavigationTab.CHATS,
            onClick = { onTabSelected(NavigationTab.CHATS) },
            icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chats") },
            label = { Text("Chats") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary
            )
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.SPACES,
            onClick = { onTabSelected(NavigationTab.SPACES) },
            icon = { Icon(Icons.Default.GridView, contentDescription = "Spaces") },
            label = { Text("Spaces") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary
            )
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.CALLS,
            onClick = { onTabSelected(NavigationTab.CALLS) },
            icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
            label = { Text("Calls") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary
            )
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.SETTINGS,
            onClick = { onTabSelected(NavigationTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun CallsTabScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "WebRTC Call Logs & Signals",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "E2EE Voice and Video calling history powered by WebRTC mesh",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsTabScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "E2EE Privacy & Key Management",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Signal Protocol Fingerprint: SIG3-FINGERPRINT-88A9-99B2-C11D",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
