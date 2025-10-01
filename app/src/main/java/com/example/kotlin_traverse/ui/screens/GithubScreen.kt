package com.example.kotlin_traverse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlin_traverse.data.model.Session
import com.example.kotlin_traverse.viewmodel.DashboardState

@Composable
fun GithubScreen(
    session: Session,
    state: DashboardState,
    onUpdateGithub: (username: String?, repo: String?, branch: String?, linked: Boolean?) -> Unit,
    onRefreshCommits: () -> Unit
) {
    val githubProfile = session.user.github
    val usernameState = rememberSaveable(githubProfile?.username) { mutableStateOf(githubProfile?.username.orEmpty()) }
    val repoState = rememberSaveable(githubProfile?.repo) { mutableStateOf(githubProfile?.repo.orEmpty()) }
    val branchState = rememberSaveable(githubProfile?.branch) { mutableStateOf(githubProfile?.branch.orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Code,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "GitHub integration",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Connect your repository to sync commits with your coding journey.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Linked repository", style = MaterialTheme.typography.titleMedium)
                if (githubProfile?.linked == true && githubProfile.username.isNotBlank() && githubProfile.repo.isNotBlank()) {
                    Text(
                        text = "Currently linked to ${githubProfile.username}/${githubProfile.repo}${githubProfile.branch.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Not connected yet. Share your repo details below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = usernameState.value,
                    onValueChange = { usernameState.value = it },
                    label = { Text("GitHub username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = repoState.value,
                    onValueChange = { repoState.value = it },
                    label = { Text("Repository") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = branchState.value,
                    onValueChange = { branchState.value = it },
                    label = { Text("Branch") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(onClick = {
                        onUpdateGithub(
                            usernameState.value.ifBlank { null },
                            repoState.value.ifBlank { null },
                            branchState.value.ifBlank { null },
                            true
                        )
                    }) {
                        Text("Save connection")
                    }
                    TextButton(onClick = {
                        usernameState.value = ""
                        repoState.value = ""
                        branchState.value = ""
                        onUpdateGithub(null, null, null, false)
                    }) {
                        Text("Unlink")
                    }
                }
            }
        }

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Recent commits", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onRefreshCommits, enabled = !state.commitsLoading) {
                        Text("Refresh")
                    }
                }
                if (state.commitsLoading) {
                    CircularProgressIndicator()
                } else if (state.commits.isEmpty()) {
                    Text("No commits yet. Save your GitHub details to start tracking activity.")
                } else {
                    HorizontalDivider()
                    state.commits.take(20).forEach { commit ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(commit.message, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = "${commit.author} · ${commit.date}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
