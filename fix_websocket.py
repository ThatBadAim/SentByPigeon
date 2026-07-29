import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/core/network/websocket/WebSocketManager.kt"
with open(filepath, "r") as f:
    content = f.read()

content = content.replace("import kotlinx.coroutines.flow.MutableStateFlow", "import kotlinx.coroutines.flow.MutableStateFlow\nimport kotlinx.coroutines.cancel")

with open(filepath, "w") as f:
    f.write(content)
