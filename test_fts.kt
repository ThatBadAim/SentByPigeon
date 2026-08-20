import androidx.room.*

@Entity(tableName = "messages_fts")
@Fts4
data class MessageFtsEntity(
    val messageId: String,
    val content: String,
    val senderName: String
)
