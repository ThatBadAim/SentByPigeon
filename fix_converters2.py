import re

with open("app/src/main/java/com/hybrid/messaging/core/database/Converters.kt", "r") as f:
    content = f.read()

content = content.replace(
    'import com.hybrid.messaging.core.model.UserStatus\n',
    'import com.hybrid.messaging.core.model.UserStatus\nimport com.hybrid.messaging.core.model.SyncState\n'
)

content = content.replace(
    '    fun toEncryptionStatus(value: String): EncryptionStatus = runCatching { EncryptionStatus.valueOf(value) }.getOrDefault(EncryptionStatus.ENCRYPTED_SIGNAL_V3)\n',
    '    fun toEncryptionStatus(value: String): EncryptionStatus = runCatching { EncryptionStatus.valueOf(value) }.getOrDefault(EncryptionStatus.ENCRYPTED_SIGNAL_V3)\n\n    @TypeConverter\n    fun fromSyncState(status: SyncState): String = status.name\n\n    @TypeConverter\n    fun toSyncState(value: String): SyncState = runCatching { SyncState.valueOf(value) }.getOrDefault(SyncState.PENDING)\n'
)

with open("app/src/main/java/com/hybrid/messaging/core/database/Converters.kt", "w") as f:
    f.write(content)
