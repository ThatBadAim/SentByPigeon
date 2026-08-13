import re

with open("app/src/main/java/com/hybrid/messaging/core/model/Models.kt", "r") as f:
    content = f.read()

content = content.replace(
    'enum class MessageType {\n',
    'enum class SyncState {\n    PENDING, SENT, DELIVERED, READ, FAILED\n}\n\nenum class MessageType {\n'
)

content = content.replace(
    '    val encryptionStatus: EncryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n    val reactions: List<Reaction> = emptyList(),',
    '    val encryptionStatus: EncryptionStatus = EncryptionStatus.ENCRYPTED_SIGNAL_V3,\n    val syncState: SyncState = SyncState.PENDING,\n    val reactions: List<Reaction> = emptyList(),'
)

with open("app/src/main/java/com/hybrid/messaging/core/model/Models.kt", "w") as f:
    f.write(content)
