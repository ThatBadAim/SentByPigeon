package com.hybrid.messaging.feature.spaces.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ServerSettingsScreen() {
    var serverName by remember { mutableStateOf("") }
    var iconUrl by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Server Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = serverName,
            onValueChange = { serverName = it },
            label = { Text("Server Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = iconUrl,
            onValueChange = { iconUrl = it },
            label = { Text("Icon URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /* Save Settings */ }, modifier = Modifier.align(Alignment.End)) {
            Text("Save")
        }
    }
}

@Composable
fun RoleManagementScreen() {
    val roles = listOf("Admin", "Moderator", "Member", "CustomRole")
    var customRoleName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Role Management", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = customRoleName,
            onValueChange = { customRoleName = it },
            label = { Text("New Custom Role Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { /* Create Role */ }, modifier = Modifier.align(Alignment.End)) {
            Text("Create Role")
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(roles) { role ->
                var switchState by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = role, style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = switchState,
                        onCheckedChange = { switchState = it }
                    )
                }
            }
        }
    }
}

@Composable
fun UserManagementScreen() {
    val users = listOf("Alice", "Bob", "Charlie", "Dave")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("User Management", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(users) { user ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = user, style = MaterialTheme.typography.bodyLarge)
                    Row {
                        TextButton(onClick = { /* Kick User */ }) {
                            Text("Kick", color = MaterialTheme.colorScheme.error)
                        }
                        TextButton(onClick = { /* Ban User */ }) {
                            Text("Ban", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelSettingsScreen() {
    var channelTopic by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Channel Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = channelTopic,
            onValueChange = { channelTopic = it },
            label = { Text("Channel Topic / Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /* Save Settings */ }, modifier = Modifier.align(Alignment.End)) {
            Text("Save")
        }
    }
}
