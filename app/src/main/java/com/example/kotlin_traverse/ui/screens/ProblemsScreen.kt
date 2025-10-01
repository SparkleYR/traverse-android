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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kotlin_traverse.data.model.Problem
import com.example.kotlin_traverse.data.model.ProblemSolved
import com.example.kotlin_traverse.viewmodel.ProblemsState
import java.time.Instant

private enum class ProblemsTab { All, Solved }

@Composable
fun ProblemsScreen(
    state: ProblemsState,
    onRefresh: () -> Unit,
    onSubmit: (
        name: String,
        platform: String,
        difficulty: Int,
        solved: ProblemSolved,
        ignored: Boolean,
        parentTopic: String,
        grandparent: String?,
        link: String
    ) -> Unit
) {
    val currentTab = rememberSaveable { mutableStateOf(ProblemsTab.All) }
    val nameState = rememberSaveable { mutableStateOf("") }
    val platformState = rememberSaveable { mutableStateOf("") }
    val linkState = rememberSaveable { mutableStateOf("") }
    val topicState = rememberSaveable { mutableStateOf("") }
    val grandparentState = rememberSaveable { mutableStateOf("") }
    val difficultyState = rememberSaveable { mutableStateOf(1) }
    val solvedState = rememberSaveable { mutableStateOf(false) }
    val ignoredState = rememberSaveable { mutableStateOf(false) }
    val triesState = rememberSaveable { mutableStateOf("1") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add a problem", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = nameState.value,
                        onValueChange = { nameState.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Problem name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = platformState.value,
                        onValueChange = { platformState.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Platform") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = topicState.value,
                        onValueChange = { topicState.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Parent topic") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = grandparentState.value,
                        onValueChange = { grandparentState.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Grandparent topic (optional)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = linkState.value,
                        onValueChange = { linkState.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Problem link") },
                        singleLine = true
                    )
                    SingleChoiceSegmentedButtonRow {
                        listOf("Easy", "Medium", "Hard").forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = difficultyState.value == index,
                                onClick = { difficultyState.value = index },
                                shape = SegmentedButtonDefaults.itemShape(index, 3)
                            ) {
                                Text(label)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Switch(checked = solvedState.value, onCheckedChange = { solvedState.value = it })
                        Text("Solved")
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(checked = ignoredState.value, onCheckedChange = { ignoredState.value = it })
                        Text("Ignored")
                    }
                    OutlinedTextField(
                        value = triesState.value,
                        onValueChange = { triesState.value = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Attempts") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                    Button(
                        onClick = {
                            if (nameState.value.isNotBlank() && platformState.value.isNotBlank() && linkState.value.isNotBlank() && topicState.value.isNotBlank()) {
                                onSubmit(
                                    nameState.value.trim(),
                                    platformState.value.trim(),
                                    difficultyState.value,
                                    ProblemSolved(
                                        value = solvedState.value,
                                        date = if (solvedState.value) Instant.now().epochSecond else null,
                                        tries = triesState.value.toIntOrNull()
                                    ),
                                    ignoredState.value,
                                    topicState.value.trim(),
                                    grandparentState.value.ifBlank { null }?.trim(),
                                    linkState.value.trim()
                                )
                                nameState.value = ""
                                platformState.value = ""
                                linkState.value = ""
                                topicState.value = ""
                                grandparentState.value = ""
                                triesState.value = "1"
                                solvedState.value = false
                                ignoredState.value = false
                                difficultyState.value = 1
                            }
                        },
                        enabled = !state.isSubmitting
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Submit")
                        }
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProblemsTab.values().forEach { tab ->
                        val isSelected = currentTab.value == tab
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = { currentTab.value = tab },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = if (isSelected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        ) {
                            Text(
                                text = if (tab == ProblemsTab.All) "All problems" else "Solved"
                            )
                        }
                    }
                }
                TextButton(onClick = onRefresh, enabled = !state.isLoading) {
                    Text("Refresh")
                }
            }
        }
        if (state.isLoading) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        } else {
            val problems = if (currentTab.value == ProblemsTab.All) state.problems else state.solvedProblems
            if (problems.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No problems yet. Submit one to get started.")
                }
            } else {
                items(problems) { problem ->
                    ProblemCard(problem)
                }
            }
        }
    }
}

@Composable
private fun ProblemCard(problem: Problem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(problem.name, style = MaterialTheme.typography.titleMedium)
            Text(problem.platform, style = MaterialTheme.typography.bodyMedium)
            Text("Difficulty: ${difficultyLabel(problem.difficulty)}")
            Text("Topic: ${problem.parentTopic}")
            if (problem.grandparent.isNotBlank()) {
                Text("Category: ${problem.grandparent}")
            }
            Text("Link: ${problem.problemLink}", color = MaterialTheme.colorScheme.primary)
            if (problem.solved.value) {
                Text("Solved")
            }
            if (problem.ignored) {
                Text("Ignored", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun difficultyLabel(value: Int) = when (value) {
    0 -> "Easy"
    1 -> "Medium"
    else -> "Hard"
}
