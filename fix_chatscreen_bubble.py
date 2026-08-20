import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/feature/chat/ui/ChatScreen.kt"
with open(filepath, "r") as f:
    content = f.read()

message_bubble_call = """                        MessageBubble(
                            message = message,
                            isUserMe = message.senderId == "user_me",
                            isPlayingVoice = uiState.playingVoiceMessageId == message.id && uiState.isPlayingVoice,
                            voicePlaybackProgress = if (uiState.playingVoiceMessageId == message.id) uiState.voicePlaybackProgress else 0f,
                            isFocused = uiState.focusedMessageId == message.id,
                            onLongClick = {"""

content = content.replace("""                        MessageBubble(
                            message = message,
                            isUserMe = message.senderId == "user_me",
                            isPlayingVoice = uiState.playingVoiceMessageId == message.id && uiState.isPlayingVoice,
                            voicePlaybackProgress = if (uiState.playingVoiceMessageId == message.id) uiState.voicePlaybackProgress else 0f,
                            onLongClick = {""", message_bubble_call)

message_bubble_def = """@Composable
fun MessageBubble(
    message: Message,
    isUserMe: Boolean,
    isPlayingVoice: Boolean,
    voicePlaybackProgress: Float,
    isFocused: Boolean = false,
    onLongClick: () -> Unit,"""

content = content.replace("""@Composable
fun MessageBubble(
    message: Message,
    isUserMe: Boolean,
    isPlayingVoice: Boolean,
    voicePlaybackProgress: Float,
    onLongClick: () -> Unit,""", message_bubble_def)


surface_def = """        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUserMe) 16.dp else 4.dp,
                bottomEnd = if (isUserMe) 4.dp else 16.dp
            ),
            color = if (isFocused) Color.Yellow.copy(alpha = 0.3f) else if (isUserMe) BubbleOutgoing else BubbleIncoming,
            tonalElevation = 1.dp
        ) {"""

content = content.replace("""        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUserMe) 16.dp else 4.dp,
                bottomEnd = if (isUserMe) 4.dp else 16.dp
            ),
            color = if (isUserMe) BubbleOutgoing else BubbleIncoming,
            tonalElevation = 1.dp
        ) {""", surface_def)


with open(filepath, "w") as f:
    f.write(content)
