import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/feature/chat/ui/ChatScreen.kt"
with open(filepath, "r") as f:
    content = f.read()

content = content.replace(
    "val bubbleColor = if (isUserMe) BubbleOutgoing else BubbleIncoming",
    "val bubbleColor = if (isFocused) Color.Yellow.copy(alpha = 0.3f) else if (isUserMe) BubbleOutgoing else BubbleIncoming"
)

with open(filepath, "w") as f:
    f.write(content)
