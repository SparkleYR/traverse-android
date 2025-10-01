package com.example.kotlin_traverse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kotlin_traverse.data.model.FriendGamification
import com.example.kotlin_traverse.viewmodel.FriendsState

private enum class LeaderboardSegment { Friends, Global }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    state: FriendsState,
    onRefresh: () -> Unit,
    onAddFriend: (String) -> Unit
) {
    val friendName = rememberSaveable { mutableStateOf("") }
    val leaderboardSelection = rememberSaveable { mutableStateOf(LeaderboardSegment.Friends) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Friends & leaderboard") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Add a friend", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = friendName.value,
                            onValueChange = { friendName.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Friend username") },
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (friendName.value.isNotBlank()) {
                                    onAddFriend(friendName.value.trim())
                                    friendName.value = ""
                                }
                            },
                            enabled = friendName.value.isNotBlank()
                        ) {
                            Text("Send request")
                        }
                        TextButton(onClick = onRefresh, enabled = !state.isLoading) {
                            Text("Refresh")
                        }
                    }
                }
            }
            if (state.isLoading) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            } else {
                item {
                    Text("Friends", style = MaterialTheme.typography.titleMedium)
                }
                if (state.friends.isEmpty()) {
                    item { Text("No friends yet. Add someone by username.") }
                } else {
                    items(state.friends) { friend ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(friend.username, style = MaterialTheme.typography.titleMedium)
                                if (friend.email.isNotBlank()) {
                                    Text(friend.email, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                item {
                    LeaderboardHeader(
                        selectedSegment = leaderboardSelection.value,
                        onSegmentChange = { leaderboardSelection.value = it }
                    )
                }

                val leaderboardEntries = when (leaderboardSelection.value) {
                    LeaderboardSegment.Friends -> state.friendGamification
                    LeaderboardSegment.Global -> emptyList()
                }

                if (leaderboardEntries.isEmpty()) {
                    item {
                        val message = if (leaderboardSelection.value == LeaderboardSegment.Friends) {
                            "No leaderboard data yet."
                        } else {
                            "Global leaderboard coming soon."
                        }
                        Text(message)
                    }
                } else {
                    itemsIndexed(leaderboardEntries.sortedBy { it.rank }) { index, entry ->
                        LeaderboardRow(position = index + 1, entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardHeader(
    selectedSegment: LeaderboardSegment,
    onSegmentChange: (LeaderboardSegment) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Leaderboard", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow {
                LeaderboardSegment.values().forEachIndexed { index, segment ->
                    SegmentedButton(
                        selected = selectedSegment == segment,
                        onClick = { onSegmentChange(segment) },
                        shape = SegmentedButtonDefaults.itemShape(index, LeaderboardSegment.values().size)
                    ) {
                        val label = if (segment == LeaderboardSegment.Global) "Global" else "Friends"
                        Text(label)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(position: Int, entry: FriendGamification) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("#$position", style = MaterialTheme.typography.titleMedium)
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initial = entry.friend.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    Text(initial, style = MaterialTheme.typography.titleMedium)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.friend.username, style = MaterialTheme.typography.titleMedium)
                Text("XP ${entry.xp} • Streak ${entry.streakDays}d", style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Lvl ${entry.level}", style = MaterialTheme.typography.labelLarge)
                Text("Rank ${entry.rank}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
