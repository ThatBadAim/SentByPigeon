package com.hybrid.messaging.feature.chat

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.hybrid.messaging.core.model.ChatRoom
import com.hybrid.messaging.core.model.ChatRoomType
import com.hybrid.messaging.core.model.Message
import com.hybrid.messaging.feature.chat.ui.MessageBubble
import org.junit.Rule
import org.junit.Test

class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun messageRendersCorrectly() {
        val testMessage = Message(
            id = "m1",
            roomId = "room1",
            senderId = "user1",
            senderName = "John Doe",
            content = "Hello Compose",
            timestamp = 0L
        )

        composeTestRule.setContent {
            MessageBubble(
                message = testMessage,
                isUserMe = false,
                isPlayingVoice = false,
                voicePlaybackProgress = 0f,
                onLongClick = {},
                onToggleVoice = {}
            )
        }

        composeTestRule.onNodeWithText("Hello Compose").assertIsDisplayed()
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    }
}