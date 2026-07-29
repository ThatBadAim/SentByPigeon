package com.hybrid.messaging.feature.chat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hybrid.messaging.core.model.ChatRoomType
import com.hybrid.messaging.core.model.Message
import com.hybrid.messaging.core.model.MessageType
import com.hybrid.messaging.core.model.EncryptionStatus
import com.hybrid.messaging.core.network.websocket.ConnectionState
import com.hybrid.messaging.feature.chat.ChatUiEvent
import com.hybrid.messaging.feature.chat.ChatUiState
import com.hybrid.messaging.feature.chat.ChatViewModel
import com.hybrid.messaging.feature.theme.AccentVoiceActive
import com.hybrid.messaging.feature.theme.BubbleIncoming
import com.hybrid.messaging.feature.theme.BubbleOutgoing
import com.hybrid.messaging.feature.theme.E2eeLockGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit = {},
    onStartCall: (isVideo: Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to bottom on new messages
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(
                roomName = uiState.room?.name ?: "general",
                isEncrypted = uiState.room?.isEncrypted ?: true,
                connectionState = uiState.connectionState,
                onStartCall = onStartCall
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = listState.canScrollForward || uiState.unreadMessagesBelow > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            if (uiState.messages.isNotEmpty()) {
                                listState.animateScrollToItem(uiState.messages.size - 1)
                            }
                            viewModel.onEvent(ChatUiEvent.ClearUnreadBadge)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    BadgedBox(
                        badge = {
                            if (uiState.unreadMessagesBelow > 0) {
                                Badge { Text("${uiState.unreadMessagesBelow}") }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Scroll to bottom")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Message List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isUserMe = message.senderId == "user_me",
                            isPlayingVoice = uiState.playingVoiceMessageId == message.id && uiState.isPlayingVoice,
                            voicePlaybackProgress = if (uiState.playingVoiceMessageId == message.id) uiState.voicePlaybackProgress else 0f,
                            onLongClick = {
                                viewModel.onEvent(ChatUiEvent.SelectMessageForReaction(message))
                            },
                            onToggleVoice = {
                                viewModel.onEvent(ChatUiEvent.ToggleVoicePlayback(message.id))
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Typing indicator bar
                if (uiState.isTyping) {
                    Text(
                        text = "${uiState.typingUserName ?: "Someone"} is typing...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Bottom Input Bar
                ChatInputBar(
                    text = uiState.inputQuery,
                    isRecording = uiState.isRecordingVoiceNote,
                    onTextChanged = { viewModel.onEvent(ChatUiEvent.UpdateInput(it)) },
                    onSendClicked = { viewModel.onEvent(ChatUiEvent.SendMessage(uiState.inputQuery)) },
                    onVoiceRecordToggle = { viewModel.onEvent(ChatUiEvent.ToggleVoiceRecording) }
                )
            }

            // iMessage Style Reaction Picker Overlay Sheet
            if (uiState.selectedMessageForReaction != null) {
                ReactionPickerSheet(
                    onDismiss = { viewModel.onEvent(ChatUiEvent.SelectMessageForReaction(null)) },
                    onEmojiSelected = { emoji ->
                        uiState.selectedMessageForReaction?.let { msg ->
                            viewModel.onEvent(ChatUiEvent.AddReaction(msg.id, emoji))
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    roomName: String,
    isEncrypted: Boolean,
    connectionState: ConnectionState,
    onStartCall: (isVideo: Boolean) -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = roomName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isEncrypted) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "E2EE Encrypted",
                                tint = E2eeLockGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "End-to-End Encrypted",
                                style = MaterialTheme.typography.labelSmall,
                                color = E2eeLockGreen
                            )
                        } else {
                            Text(
                                text = connectionState.name.lowercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = { onStartCall(false) }) {
                Icon(Icons.Default.Call, contentDescription = "Audio Call")
            }
            IconButton(onClick = { onStartCall(true) }) {
                Icon(Icons.Default.Videocam, contentDescription = "Video Call")
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
        }
    )
}

@Composable
fun MessageBubble(
    message: Message,
    isUserMe: Boolean,
    isPlayingVoice: Boolean,
    voicePlaybackProgress: Float,
    onLongClick: () -> Unit,
    onToggleVoice: () -> Unit
) {
    val bubbleColor = if (isUserMe) BubbleOutgoing else BubbleIncoming
    val alignment = if (isUserMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUserMe) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            shape = shape,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clickable { onLongClick() }
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!isUserMe) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                when (message.messageType) {
                    MessageType.VOICE_NOTE -> {
                        VoiceNotePlayerUI(
                            durationMs = message.audioDurationMs ?: 0L,
                            isPlaying = isPlayingVoice,
                            progress = voicePlaybackProgress,
                            onTogglePlay = onToggleVoice
                        )
                    }
                    else -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )

                    if (message.encryptionStatus == EncryptionStatus.ENCRYPTED_SIGNAL_V3) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    if (isUserMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "✓✓",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Reaction Overlays
                if (message.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        message.reactions.forEach { reaction ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "${reaction.emoji} ${reaction.count}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceNotePlayerUI(
    durationMs: Long,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(220.dp)
            .padding(vertical = 4.dp)
    ) {
        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier
                .size(36.dp)
                .background(AccentVoiceActive, CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play voice note",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AccentVoiceActive,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${durationMs / 1000}s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    isRecording: Boolean,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onVoiceRecordToggle: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Add, contentDescription = "Add attachment", tint = MaterialTheme.colorScheme.primary)
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = { Text("Message #general...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(4.dp))

            if (text.isNotBlank()) {
                IconButton(
                    onClick = onSendClicked,
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            } else {
                IconButton(
                    onClick = onVoiceRecordToggle,
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (isRecording) Color.Red else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Record",
                        tint = if (isRecording) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionPickerSheet(
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val quickEmojis = listOf("❤️", "👍", "🔥", "😂", "😮", "🚀", "👏", "💯")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reactions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                quickEmojis.forEach { emoji ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                onEmojiSelected(emoji)
                                onDismiss()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
