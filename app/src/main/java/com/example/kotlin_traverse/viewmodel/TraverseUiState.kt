package com.example.kotlin_traverse.viewmodel

import com.example.kotlin_traverse.data.model.AdminUser
import com.example.kotlin_traverse.data.model.Friend
import com.example.kotlin_traverse.data.model.FriendGamification
import com.example.kotlin_traverse.data.model.Gamification
import com.example.kotlin_traverse.data.model.GitCommit
import com.example.kotlin_traverse.data.model.Problem
import com.example.kotlin_traverse.data.model.Session

enum class AuthMode { Login, Register }

data class TraverseUiState(
    val isDarkTheme: Boolean = false,
    val session: Session? = null,
    val authState: AuthState = AuthState(),
    val dashboardState: DashboardState = DashboardState(),
    val problemsState: ProblemsState = ProblemsState(),
    val friendsState: FriendsState = FriendsState(),
    val adminState: AdminState = AdminState()
)

data class AuthState(
    val mode: AuthMode = AuthMode.Login,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DashboardState(
    val isLoading: Boolean = false,
    val gamification: Gamification? = null,
    val commits: List<GitCommit> = emptyList(),
    val commitsLoading: Boolean = false
)

data class ProblemsState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val problems: List<Problem> = emptyList(),
    val solvedProblems: List<Problem> = emptyList()
)

data class FriendsState(
    val isLoading: Boolean = false,
    val friends: List<Friend> = emptyList(),
    val friendGamification: List<FriendGamification> = emptyList()
)

data class AdminState(
    val isLoading: Boolean = false,
    val users: List<AdminUser> = emptyList()
)

sealed interface UiEvent {
    data class SnackbarMessage(val message: String) : UiEvent
}
