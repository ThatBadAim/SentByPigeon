import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/core/database/entity/MessageFtsEntity.kt"
with open(filepath, "r") as f:
    content = f.read()

content = content.replace("@Entity(tableName = \"messages_fts\")", "@Entity(tableName = \"messages_fts\")")
with open(filepath, "w") as f:
    f.write(content)
