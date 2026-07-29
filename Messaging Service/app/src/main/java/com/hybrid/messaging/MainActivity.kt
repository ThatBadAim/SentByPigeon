package com.hybrid.messaging

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import android.content.Intent
import androidx.activity.viewModels
import com.hybrid.messaging.feature.main.ui.MainScaffold
import com.hybrid.messaging.feature.theme.NexusMessagingTheme
import com.hybrid.messaging.feature.spaces.SpaceViewModel
import com.hybrid.messaging.feature.spaces.SpaceUiEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val spaceViewModel: SpaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            NexusMessagingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScaffold()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.host == "app.hybrid.messaging" && data.path?.startsWith("/invite/") == true) {
            val inviteCode = data.pathSegments.lastOrNull()
            if (inviteCode != null) {
                spaceViewModel.onEvent(SpaceUiEvent.JoinServer(inviteCode))
            }
        }
    }
}
