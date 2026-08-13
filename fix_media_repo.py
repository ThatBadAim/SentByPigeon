import re

with open("app/src/main/java/com/hybrid/messaging/core/data/repository/MessageRepositoryImpl.kt", "r") as f:
    content = f.read()

content = content.replace(
    '                    messageType = MessageType.VOICE_NOTE.name,\n                    mediaUrl = audioFilePath,\n                    audioDurationMs = durationMs,\n',
    '                    messageType = type.name,\n                    mediaUrl = mediaUrl,\n'
)

with open("app/src/main/java/com/hybrid/messaging/core/data/repository/MessageRepositoryImpl.kt", "w") as f:
    f.write(content)
