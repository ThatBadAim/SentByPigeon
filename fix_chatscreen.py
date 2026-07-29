import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/feature/chat/ui/ChatScreen.kt"
with open(filepath, "r") as f:
    content = f.read()

scroll_logic = """    // Scroll to bottom on new messages or specific message
    LaunchedEffect(uiState.messages.size, uiState.focusedMessageId) {
        if (uiState.focusedMessageId != null) {
            val index = uiState.messages.indexOfFirst { it.id == uiState.focusedMessageId }
            if (index != -1) {
                listState.animateScrollToItem(index)
            }
        } else if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }"""

content = content.replace("""    // Scroll to bottom on new messages
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }""", scroll_logic)

with open(filepath, "w") as f:
    f.write(content)
