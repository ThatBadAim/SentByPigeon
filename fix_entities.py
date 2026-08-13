import re

with open("app/src/main/java/com/hybrid/messaging/core/database/entity/Entities.kt", "r") as f:
    content = f.read()

content = content.replace(
    'import com.hybrid.messaging.core.model.UserStatus\n',
    'import com.hybrid.messaging.core.model.UserStatus\nimport com.hybrid.messaging.core.model.SyncState\nimport androidx.room.ColumnInfo\n'
)

content = content.replace(
    '    val encryptionStatus: EncryptionStatus,\n    val replyToMessageId: String?',
    '    val encryptionStatus: EncryptionStatus,\n    @ColumnInfo(defaultValue = "\'PENDING\'")\n    val syncState: SyncState = SyncState.PENDING,\n    val replyToMessageId: String?'
)

with open("app/src/main/java/com/hybrid/messaging/core/database/entity/Entities.kt", "w") as f:
    f.write(content)
