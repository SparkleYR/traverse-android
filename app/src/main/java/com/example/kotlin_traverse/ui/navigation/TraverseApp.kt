package com.example.kotlin_traverse.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kotlin_traverse.core.TraverseAppContainer
import com.example.kotlin_traverse.ui.components.TraverseTopBar
import com.example.kotlin_traverse.ui.screens.AuthScreen
import com.example.kotlin_traverse.ui.screens.HomeScreen
import com.example.kotlin_traverse.ui.theme.TraverseTheme
import com.example.kotlin_traverse.viewmodel.UiEvent
import com.example.kotlin_traverse.viewmodel.TraverseViewModel

@Composable
fun TraverseApp(container: TraverseAppContainer) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel: TraverseViewModel = viewModel(factory = container.viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.session) {
        val destination = if (uiState.session == null) TraverseDestination.Auth.route else TraverseDestination.Home.route
        if (navController.currentDestination?.route != destination) {
            navController.navigate(destination) {
                val startId = navController.graph.findStartDestination().id
                if (startId != 0) {
                    popUpTo(startId) { inclusive = true }
                }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.SnackbarMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    TraverseTheme(darkTheme = uiState.isDarkTheme) {
        Scaffold(
            topBar = {
                TraverseTopBar(
                    isAuthenticated = uiState.session != null,
                    isDarkTheme = uiState.isDarkTheme,
                    onToggleTheme = viewModel::toggleTheme,
                    onLogout = viewModel::logout
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = TraverseDestination.Auth.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(TraverseDestination.Auth.route) {
                    AuthScreen(
                        state = uiState.authState,
                        onLogin = viewModel::login,
                        onRegister = viewModel::register,
                        onToggleMode = viewModel::toggleAuthMode
                    )
                }
                composable(TraverseDestination.Home.route) {
                    HomeScreen(
                        uiState = uiState,
                        onRefreshDashboard = viewModel::refreshDashboard,
                        onRefreshCommits = viewModel::refreshCommits,
                        onRefreshProblems = viewModel::refreshProblems,
                        onSubmitProblem = viewModel::submitProblem,
                        onRefreshFriends = viewModel::refreshFriends,
                        onAddFriend = viewModel::addFriend,
                        onRefreshAdmin = viewModel::refreshAdmin,
                        onDeleteUser = viewModel::deleteUser,
                        onUpdateGithub = viewModel::updateGithub
                    )
                }
            }
        }
    }
}
