package com.example.kotlin_traverse.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.kotlin_traverse.data.model.ProblemSolved
import com.example.kotlin_traverse.ui.components.TraverseBottomBar
import com.example.kotlin_traverse.ui.navigation.HomeSection
import com.example.kotlin_traverse.viewmodel.TraverseUiState

@Composable
fun HomeScreen(
    uiState: TraverseUiState,
    onRefreshDashboard: () -> Unit,
    onRefreshCommits: () -> Unit,
    onRefreshProblems: () -> Unit,
    onSubmitProblem: (String, String, Int, ProblemSolved, Boolean, String, String?, String) -> Unit,
    onRefreshFriends: () -> Unit,
    onAddFriend: (String) -> Unit,
    onRefreshAdmin: () -> Unit,
    onDeleteUser: (String) -> Unit,
    onUpdateGithub: (String?, String?, String?, Boolean?) -> Unit
) {
    val session = uiState.session ?: return
    val sections = remember(session.user.isAdmin) {
        if (session.user.isAdmin) {
            listOf(HomeSection.Dashboard, HomeSection.Problems, HomeSection.Integrations, HomeSection.Friends, HomeSection.Admin)
        } else {
            listOf(HomeSection.Dashboard, HomeSection.Problems, HomeSection.Integrations, HomeSection.Friends)
        }
    }
    var currentSection by rememberSaveable(session.user.isAdmin) { mutableStateOf(sections.first()) }

    LaunchedEffect(session.user.isAdmin) {
        if (!session.user.isAdmin && currentSection == HomeSection.Admin) {
            currentSection = HomeSection.Dashboard
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (currentSection) {
                HomeSection.Dashboard -> DashboardScreen(
                    session = session,
                    state = uiState.dashboardState,
                    solvedProblems = uiState.problemsState.solvedProblems,
                    onRefreshGamification = onRefreshDashboard,
                    onRefreshCommits = onRefreshCommits
                )
                HomeSection.Problems -> ProblemsScreen(
                    state = uiState.problemsState,
                    onRefresh = onRefreshProblems,
                    onSubmit = onSubmitProblem
                )
                HomeSection.Integrations -> GithubScreen(
                    session = session,
                    state = uiState.dashboardState,
                    onUpdateGithub = onUpdateGithub,
                    onRefreshCommits = onRefreshCommits
                )
                HomeSection.Friends -> FriendsScreen(
                    state = uiState.friendsState,
                    onRefresh = onRefreshFriends,
                    onAddFriend = onAddFriend
                )
                HomeSection.Admin -> AdminScreen(
                    state = uiState.adminState,
                    currentUserId = session.user.id,
                    onRefresh = onRefreshAdmin,
                    onDelete = onDeleteUser
                )
            }
        }
        TraverseBottomBar(
            sections = sections,
            current = currentSection,
            onSelect = { currentSection = it }
        )
    }
}
