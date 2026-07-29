import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/core/database/dao/Daos.kt"
with open(filepath, "r") as f:
    content = f.read()

# For FTS tables, we join on the rowid of the FTS table with rowid of the main table if we use Fts4 with contentEntity, but since we are not, we just use the column names. In Fts4 tables, the string `messageId` should exist if defined. Let's make sure it's created correctly. Ah, in Room, if it's an FTS table and we use an explicit string, it's just a column. Wait, room maps FTS queries carefully. Let's use `rowid` for deletion if needed, but since we defined messageId, it should exist. The problem is `messages_fts.messageId` might not be exposed as a queryable column in FTS if not carefully configured, or Room might be using the column name as is but without table prefix? No, it's `messages_fts`.
# Wait, room complains about `messageId` missing in `deleteMessageFtsEntity`. In FTS4, we cannot just delete by a custom column. FTS4 only supports rowid deletion or we have to use `MATCH`. Actually, deleting from FTS4 table is done by `MATCH` or `rowid`.
# Wait, if we use `@Fts4(contentEntity = MessageEntity::class)`, Room creates triggers for INSERT, UPDATE, DELETE automatically! Then we don't need manual inserts. Let's use `contentEntity = MessageEntity::class`!

content = content.replace("DELETE FROM messages_fts WHERE messageId = :messageId", "DELETE FROM messages_fts WHERE rowid = (SELECT rowid FROM messages WHERE id = :messageId)")
content = content.replace("ON messages.id = messages_fts.messageId", "ON messages.rowid = messages_fts.rowid")

with open(filepath, "w") as f:
    f.write(content)

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/core/database/entity/MessageFtsEntity.kt"
with open(filepath, "w") as f:
    f.write("""package com.hybrid.messaging.core.database.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = MessageEntity::class)
@Entity(tableName = "messages_fts")
data class MessageFtsEntity(
    val content: String,
    val senderName: String
)
""")
