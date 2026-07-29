package com.hybrid.messaging

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hybrid.messaging.feature.main.ui.MainScaffold
import com.hybrid.messaging.feature.theme.NexusMessagingTheme
import com.hybrid.messaging.feature.chat.ui.BiometricAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private var isAuthenticated by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BiometricAuth.authenticate(
            context = this,
            onSuccess = {
                isAuthenticated = true
            },
            onError = { error ->
                Toast.makeText(this, "Authentication failed: $error", Toast.LENGTH_SHORT).show()
                finish() // Close app if auth fails
            }
        )

        setContent {
            NexusMessagingTheme {
                if (!isAuthenticated) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {}
                    return@NexusMessagingTheme
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScaffold()
                }
            }
        }
    }
}
