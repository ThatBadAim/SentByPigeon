import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/core/database/Converters.kt"
with open(filepath, "r") as f:
    content = f.read()

content = content.replace("@TypeConverter\n    fromUserStatus", "@TypeConverter\n    fun fromUserStatus")
content = content.replace("@TypeConverter\n    toUserStatus", "@TypeConverter\n    fun toUserStatus")
content = content.replace("@TypeConverter\n    fromChatRoomType", "@TypeConverter\n    fun fromChatRoomType")
content = content.replace("@TypeConverter\n    toChatRoomType", "@TypeConverter\n    fun toChatRoomType")
content = content.replace("@TypeConverter\n    fromMessageType", "@TypeConverter\n    fun fromMessageType")
content = content.replace("@TypeConverter\n    toMessageType", "@TypeConverter\n    fun toMessageType")
content = content.replace("@TypeConverter\n    fromEncryptionStatus", "@TypeConverter\n    fun fromEncryptionStatus")
content = content.replace("@TypeConverter\n    toEncryptionStatus", "@TypeConverter\n    fun toEncryptionStatus")
content = content.replace("@TypeConverter\n    fromListString", "@TypeConverter\n    fun fromListString")
content = content.replace("@TypeConverter\n    toListString", "@TypeConverter\n    fun toListString")

with open(filepath, "w") as f:
    f.write(content)
