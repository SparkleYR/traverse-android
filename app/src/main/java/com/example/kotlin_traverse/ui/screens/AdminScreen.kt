package com.example.kotlin_traverse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kotlin_traverse.viewmodel.AdminState

@Composable
fun AdminScreen(
    state: AdminState,
    currentUserId: String,
    onRefresh: () -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("User management", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onRefresh, enabled = !state.isLoading) {
                Text("Refresh")
            }
        }
        if (state.isLoading) {
            item { CircularProgressIndicator() }
        } else if (state.users.isEmpty()) {
            item { Text("No users found") }
        } else {
            items(state.users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(user.username, style = MaterialTheme.typography.titleMedium)
                        Text(user.email, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        RowWithAction(
                            canDelete = user.id != currentUserId,
                            onDelete = { onDelete(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowWithAction(canDelete: Boolean, onDelete: () -> Unit) {
    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onDelete, enabled = canDelete) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete user")
        }
    }
}
