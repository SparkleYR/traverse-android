package com.example.kotlin_traverse.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlin_traverse.data.model.Problem
import com.example.kotlin_traverse.data.model.Session
import com.example.kotlin_traverse.viewmodel.DashboardState
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    session: Session,
    state: DashboardState,
    solvedProblems: List<Problem>,
    onRefreshGamification: () -> Unit,
    onRefreshCommits: () -> Unit
) {
    val githubProfile = session.user.github
    val platformSummary = solvedProblems.groupBy { problem -> problem.platform.ifBlank { "Other" } }
        .map { entry -> entry.key to entry.value.size }
        .sortedByDescending { it.second }
    val difficultyLabels = mapOf(0 to "Beginner", 1 to "Intermediate", 2 to "Advanced")
    val difficultySummary = solvedProblems.groupBy { it.difficulty }
        .map { entry ->
            val label = difficultyLabels[entry.key] ?: "Level ${entry.key}"
            label to entry.value.size
        }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }
    val chartPalette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.72f)
    )

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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Hello, ${session.user.username}", style = MaterialTheme.typography.titleLarge)
                    Text(text = session.user.email, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRefreshGamification, enabled = !state.isLoading) {
                        Text("Refresh stats")
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Level Spotlight",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (state.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        state.gamification?.let { gam ->
                            Text(
                                text = "Level ${gam.level}",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${gam.xp} XP earned",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val currentLevelFloor = (gam.level * 100).coerceAtLeast(0)
                            val nextLevelFloor = ((gam.level + 1) * 100).coerceAtLeast(currentLevelFloor + 1)
                            val progressFraction = (gam.xp - currentLevelFloor).coerceAtLeast(0)
                                .toFloat() / (nextLevelFloor - currentLevelFloor).toFloat()
                            LinearProgressIndicator(
                                progress = { progressFraction.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )
                            val remaining = nextLevelFloor - gam.xp
                            Text(
                                text = if (remaining <= 0) "You've unlocked the next level!" else "$remaining XP to reach level ${gam.level + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } ?: run {
                            Text(
                                text = "No stats yet. Solve a problem to get started!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Solved by platform", style = MaterialTheme.typography.titleMedium)
                    if (platformSummary.isEmpty()) {
                        Text(
                            text = "Solve problems across platforms to unlock this insight.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val topPlatforms = platformSummary.take(6)
                        val legendData = topPlatforms.mapIndexed { index, (platform, count) ->
                            LegendEntry(
                                label = "$platform · $count",
                                color = chartPalette[index % chartPalette.size]
                            )
                        }
                        PlatformBarChart(
                            data = topPlatforms,
                            colors = chartPalette,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            legendData.forEach { entry ->
                                LegendChip(entry)
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Difficulty mix", style = MaterialTheme.typography.titleMedium)
                    if (difficultySummary.isEmpty()) {
                        Text(
                            text = "Work through some practice to see your difficulty breakdown.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val legendEntries = difficultySummary.mapIndexed { index, (label, count) ->
                            LegendEntry(
                                label = "$label · $count",
                                color = chartPalette[index % chartPalette.size]
                            )
                        }
                        DifficultyPieChart(
                            data = difficultySummary,
                            colors = chartPalette,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            legendEntries.forEach { entry ->
                                LegendChip(entry)
                            }
                        }
                    }
                }
            }
        }
        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Daily streak", style = MaterialTheme.typography.titleMedium)
                    }
                    if (state.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        val streak = state.gamification?.streakDays ?: 0
                        Text(
                            text = "$streak day${if (streak == 1) "" else "s"} strong",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = if (streak > 0) "Keep it going with another submission today" else "Solve a problem today to start your streak",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        if (!state.isLoading && (state.gamification?.badges?.isNotEmpty() == true || state.gamification?.rank != null)) {
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Achievements", style = MaterialTheme.typography.titleMedium)
                        }
                        state.gamification?.badges?.takeIf { it.isNotEmpty() }?.let { badges ->
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                badges.forEach { badge ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(badge) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors()
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Global rank", style = MaterialTheme.typography.bodyLarge)
                        }
                        Text(
                            text = state.gamification?.rank?.let { "You\u2019re currently ranked #$it" } ?: "Rank pending",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Recent commits", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onRefreshCommits, enabled = !state.commitsLoading) {
                            Text("Refresh")
                        }
                    }
                    githubProfile?.takeIf { it.username.isNotBlank() && it.repo.isNotBlank() }?.let { profile ->
                        Text(
                            text = "${profile.username}/${profile.repo}${profile.branch?.let { " · $it" } ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (state.commitsLoading) {
                        CircularProgressIndicator()
                    } else if (state.commits.isEmpty()) {
                        Text("Head to Integrations to connect GitHub and start tracking commits.")
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        state.commits.take(20).forEach { commit ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(commit.message, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = "${commit.author} \u00B7 ${commit.date}",
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
}

private data class LegendEntry(val label: String, val color: Color)

@Composable
private fun LegendChip(entry: LegendEntry) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(entry.color, CircleShape)
        )
        Text(
            text = entry.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlatformBarChart(
    data: List<Pair<String, Int>>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.second }.coerceAtLeast(1)
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val density = LocalDensity.current
    val axisStroke = with(density) { 1.dp.toPx() }
    val labelStyle = MaterialTheme.typography.bodySmall
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val chartHeight = 160.dp
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val barCount = data.size
            if (barCount == 0) return@Canvas

            val segmentWidth = size.width / (barCount * 2f)
            val barWidth = segmentWidth
            data.forEachIndexed { index, (_, value) ->
                val color = colors[index % colors.size]
                val barHeight = size.height * (value / maxValue.toFloat())
                val left = segmentWidth / 2f + index * segmentWidth * 2f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 4f, barWidth / 4f)
                )
            }
            drawLine(
                color = axisColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = axisStroke
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { (label, _) ->
                Text(
                    text = label,
                    style = labelStyle,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f, fill = true).padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DifficultyPieChart(
    data: List<Pair<String, Int>>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val total = data.sumOf { it.second }
    if (total <= 0) return

    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val density = LocalDensity.current
    val outlineStroke = with(density) { 1.5.dp.toPx() }
    val centerLabel = "$total solved"

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = min(size.width, size.height)
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            var startAngle = -90f

            data.forEachIndexed { index, (_, value) ->
                val sweep = 360f * (value / total.toFloat())
                val color = colors[index % colors.size]
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = topLeft,
                    size = Size(diameter, diameter)
                )
                startAngle += sweep
            }

            drawArc(
                color = outlineColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = outlineStroke)
            )
        }

        Text(
            text = centerLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
