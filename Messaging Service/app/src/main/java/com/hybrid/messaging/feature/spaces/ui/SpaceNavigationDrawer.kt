package com.hybrid.messaging.feature.spaces.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hybrid.messaging.core.model.ChannelCategory
import com.hybrid.messaging.core.model.ChatRoom
import com.hybrid.messaging.core.model.ChatRoomType
import com.hybrid.messaging.core.model.Server
import com.hybrid.messaging.feature.spaces.SpaceUiEvent
import com.hybrid.messaging.feature.spaces.SpaceViewModel
import com.hybrid.messaging.feature.theme.AccentGreen
import com.hybrid.messaging.feature.theme.DarkRailBackground

@Composable
fun SpaceNavigationDrawer(
    viewModel: SpaceViewModel,
    onChannelClick: (ChatRoom) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Server Rail (Discord Style Icons Column)
        ServerRail(
            servers = uiState.servers,
            selectedServerId = uiState.selectedServerId,
            onServerSelected = { viewModel.onEvent(SpaceUiEvent.SelectServer(it)) },
            onCreateServer = { viewModel.onEvent(SpaceUiEvent.CreateServer("New Server")) }
        )

        // Right Channel Sidebar (Nested Categories & Channels)
        ChannelSidebar(
            server = uiState.selectedServer,
            selectedChannelId = uiState.selectedChannelId,
            connectedVoiceChannel = uiState.connectedVoiceChannel,
            isMicMuted = uiState.isMicMuted,
            isDeafened = uiState.isDeafened,
            onChannelClick = { channel ->
                viewModel.onEvent(SpaceUiEvent.SelectChannel(channel))
                if (channel.type == ChatRoomType.SPACE_TEXT_CHANNEL) {
                    onChannelClick(channel)
                }
            },
            onDisconnectVoice = { viewModel.onEvent(SpaceUiEvent.DisconnectVoice) },
            onToggleMic = { viewModel.onEvent(SpaceUiEvent.ToggleMic) },
            onToggleDeafen = { viewModel.onEvent(SpaceUiEvent.ToggleDeafen) }
        )
    }
}

@Composable
fun ServerRail(
    servers: List<Server>,
    selectedServerId: String?,
    onServerSelected: (String) -> Unit,
    onCreateServer: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight(),
        color = DarkRailBackground
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxHeight()
        ) {
            // Direct Messages / Home Server Icon
            ServerIconButton(
                name = "DM",
                isSelected = selectedServerId == null,
                onClick = { }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider(
                modifier = Modifier
                    .width(32.dp)
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Server Icons List
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(servers, key = { it.id }) { server ->
                    ServerIconButton(
                        name = server.name,
                        isSelected = server.id == selectedServerId,
                        onClick = { onServerSelected(server.id) }
                    )
                }

                item {
                    // Add Server Button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onCreateServer() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Server",
                                tint = AccentGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServerIconButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
    val shape = if (isSelected) RoundedCornerShape(16.dp) else CircleShape
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Active pill bar indicator
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(if (isSelected) 36.dp else 0.dp)
                .clip(RoundedCornerShape(0.dp, 4.dp, 4.dp, 0.dp))
                .background(Color.White)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Surface(
            shape = shape,
            color = containerColor,
            modifier = Modifier
                .size(48.dp)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ChannelSidebar(
    server: Server?,
    selectedChannelId: String?,
    connectedVoiceChannel: ChatRoom?,
    isMicMuted: Boolean,
    isDeafened: Boolean,
    onChannelClick: (ChatRoom) -> Unit,
    onDisconnectVoice: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleDeafen: () -> Unit
) {
    val categoryExpandState = remember { mutableStateMapOf<String, Boolean>() }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Server Header
            ServerHeader(serverName = server?.name ?: "Select Space")

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Categories & Channel List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                server?.categories?.forEach { category ->
                    val isExpanded = categoryExpandState.getOrDefault(category.id, true)

                    item(key = category.id) {
                        CategoryHeaderItem(
                            categoryName = category.name,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                categoryExpandState[category.id] = !isExpanded
                            }
                        )
                    }

                    if (isExpanded) {
                        items(category.channels, key = { it.id }) { channel ->
                            ChannelListItem(
                                channel = channel,
                                isSelected = channel.id == selectedChannelId,
                                isConnectedVoice = channel.id == connectedVoiceChannel?.id,
                                onClick = { onChannelClick(channel) }
                            )
                        }
                    }
                }
            }

            // Voice Active Status Banner
            if (connectedVoiceChannel != null) {
                VoiceActiveStatusBanner(
                    channelName = connectedVoiceChannel.name,
                    onDisconnect = onDisconnectVoice
                )
            }

            // User Profile Footer
            UserProfileFooter(
                username = "Alex Mercer",
                statusMessage = "Online",
                isMicMuted = isMicMuted,
                isDeafened = isDeafened,
                onToggleMic = onToggleMic,
                onToggleDeafen = onToggleDeafen
            )
        }
    }
}

@Composable
fun ServerHeader(serverName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = serverName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = "Server menu",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CategoryHeaderItem(
    categoryName: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .padding(top = 12.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = categoryName,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
fun ChannelListItem(
    channel: ChatRoom,
    isSelected: Boolean,
    isConnectedVoice: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.surfaceVariant
        isConnectedVoice -> AccentGreen.copy(alpha = 0.15f)
        else -> Color.Transparent
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onSurface
        isConnectedVoice -> AccentGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (channel.type == ChatRoomType.SPACE_VOICE_ROOM) Icons.Default.VolumeUp else Icons.Default.Tag,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected || isConnectedVoice) FontWeight.Bold else FontWeight.Normal
                ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun VoiceActiveStatusBanner(
    channelName: String,
    onDisconnect: () -> Unit
) {
    Surface(
        color = AccentGreen.copy(alpha = 0.2f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Voice Connected",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentGreen
                )
                Text(
                    text = channelName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onDisconnect) {
                Icon(Icons.Default.CallEnd, contentDescription = "Disconnect voice", tint = Color.Red)
            }
        }
    }
}

@Composable
fun UserProfileFooter(
    username: String,
    statusMessage: String,
    isMicMuted: Boolean,
    isDeafened: Boolean,
    onToggleMic: () -> Unit,
    onToggleDeafen: () -> Unit
) {
    Surface(
        color = DarkRailBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar with Online Dot
            Box {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("AM", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AccentGreen)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onToggleMic, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mic toggle",
                    tint = if (isMicMuted) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onToggleDeafen, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (isDeafened) Icons.Default.HeadsetOff else Icons.Default.Headset,
                    contentDescription = "Deafen toggle",
                    tint = if (isDeafened) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
