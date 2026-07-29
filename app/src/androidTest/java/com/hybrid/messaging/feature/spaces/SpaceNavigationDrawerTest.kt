package com.hybrid.messaging.feature.spaces

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.hybrid.messaging.core.model.Server
import com.hybrid.messaging.feature.spaces.ui.SpaceNavigationDrawer
import org.junit.Rule
import org.junit.Test

class SpaceNavigationDrawerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun serversAreRenderedInRail() {
        val servers = listOf(
            Server(id = "srv_1", name = "Test Server 1", ownerId = "owner"),
            Server(id = "srv_2", name = "Test Server 2", ownerId = "owner")
        )

        composeTestRule.setContent {
            com.hybrid.messaging.feature.spaces.ui.ServerRail(
                servers = servers,
                selectedServerId = "srv_1",
                onServerSelected = {},
                onCreateServer = {}
            )
        }

        // Assert nodes (servers are usually represented by abbreviated letters of their name, or their name if visible)
        composeTestRule.onNodeWithText("TS").assertIsDisplayed() // Server 1 'Test Server 1' abbreviation
    }
}