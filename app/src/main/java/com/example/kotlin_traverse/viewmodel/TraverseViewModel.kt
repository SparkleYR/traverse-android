package com.example.kotlin_traverse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_traverse.data.local.TokenStore
import com.example.kotlin_traverse.data.model.GithubProfile
import com.example.kotlin_traverse.data.model.ProblemSolved
import com.example.kotlin_traverse.data.model.Session
import com.example.kotlin_traverse.data.remote.ProblemSolvedPayload
import com.example.kotlin_traverse.data.remote.PushProblemRequest
import com.example.kotlin_traverse.data.repository.TraverseRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update

class TraverseViewModel(
    private val repository: TraverseRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(TraverseUiState())
    val uiState: StateFlow<TraverseUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            tokenStore.preferencesFlow.collectLatest { prefs ->
                val previousSession = _uiState.value.session
                _uiState.update {
                    it.copy(
                        session = prefs.session,
                        isDarkTheme = prefs.isDarkTheme
                    )
                }
                val currentSession = prefs.session
                if (currentSession?.token != previousSession?.token) {
                    if (currentSession != null) {
                        refreshAll()
                    } else {
                        clearData()
                    }
                }
            }
        }
    }

    fun toggleAuthMode() {
        val current = _uiState.value.authState
        val newMode = if (current.mode == AuthMode.Login) AuthMode.Register else AuthMode.Login
        _uiState.update { it.copy(authState = current.copy(mode = newMode, error = null)) }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            setAuthLoading(true)
            repository.login(email, password)
                .onSuccess { session ->
                    repository.persistSession(session)
                    setAuthLoading(false)
                    emitSnackbar("Welcome back, ${session.user.username}!")
                }
                .onFailure { error ->
                    setAuthError(error.message ?: "Login failed")
                }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        githubUsername: String?,
        githubRepo: String?,
        githubBranch: String?
    ) {
        viewModelScope.launch {
            setAuthLoading(true)
            repository.register(username, email, password, githubUsername, githubRepo, githubBranch)
                .onSuccess { session ->
                    repository.persistSession(session)
                    setAuthLoading(false)
                    emitSnackbar("Account created. Welcome, ${session.user.username}!")
                }
                .onFailure { error ->
                    setAuthError(error.message ?: "Registration failed")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearSession()
            clearData()
            emitSnackbar("Signed out")
        }
    }

    fun toggleTheme() {
        val target = !_uiState.value.isDarkTheme
        _uiState.update { it.copy(isDarkTheme = target) }
        viewModelScope.launch { repository.setDarkTheme(target) }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(dashboardState = it.dashboardState.copy(isLoading = true)) }
            repository.fetchGamification()
                .onSuccess { gam ->
                    _uiState.update {
                        it.copy(dashboardState = it.dashboardState.copy(isLoading = false, gamification = gam))
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(dashboardState = it.dashboardState.copy(isLoading = false)) }
                    emitSnackbar(error.message ?: "Failed to load gamification")
                }
        }
    }

    fun refreshCommits() {
        viewModelScope.launch {
            _uiState.update { it.copy(dashboardState = it.dashboardState.copy(commitsLoading = true)) }
            repository.fetchGitCommits()
                .onSuccess { commits ->
                    _uiState.update {
                        it.copy(dashboardState = it.dashboardState.copy(commitsLoading = false, commits = commits))
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(dashboardState = it.dashboardState.copy(commitsLoading = false)) }
                    emitSnackbar(error.message ?: "Failed to load Git commits")
                }
        }
    }

    fun refreshProblems() {
        viewModelScope.launch {
            _uiState.update { it.copy(problemsState = it.problemsState.copy(isLoading = true)) }
            val allResult = repository.fetchProblems(page = 1, limit = 50)
            val solvedResult = repository.fetchSolvedProblems(page = 1, limit = 50)
            val allProblems = allResult.getOrNull()?.problems ?: emptyList()
            val solvedProblems = solvedResult.getOrNull()?.problems ?: emptyList()

            if (allResult.isSuccess && solvedResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        problemsState = it.problemsState.copy(
                            isLoading = false,
                            problems = allProblems,
                            solvedProblems = solvedProblems
                        )
                    )
                }
            } else {
                _uiState.update { it.copy(problemsState = it.problemsState.copy(isLoading = false)) }
                emitSnackbar(allResult.exceptionOrNull()?.message ?: solvedResult.exceptionOrNull()?.message
                    ?: "Failed to load problems")
            }
        }
    }

    fun submitProblem(
        name: String,
        platform: String,
        difficulty: Int,
        solved: ProblemSolved,
        ignored: Boolean,
        parentTopic: String,
        grandparent: String?,
        link: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(problemsState = it.problemsState.copy(isSubmitting = true)) }
            repository.pushProblem(
                PushProblemRequest(
                    name = name,
                    platform = platform,
                    difficulty = difficulty,
                    solved = ProblemSolvedPayload(
                        value = solved.value,
                        date = solved.date,
                        tries = solved.tries
                    ),
                    ignored = ignored,
                    parentTopic = parentTopic,
                    grandparent = grandparent?.ifBlank { null },
                    problemLink = link
                )
            ).onSuccess { message ->
                _uiState.update { it.copy(problemsState = it.problemsState.copy(isSubmitting = false)) }
                emitSnackbar(message.ifBlank { "Problem submitted" })
                refreshProblems()
                refreshDashboard()
            }.onFailure { error ->
                _uiState.update { it.copy(problemsState = it.problemsState.copy(isSubmitting = false)) }
                emitSnackbar(error.message ?: "Could not submit problem")
            }
        }
    }

    fun refreshFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(friendsState = it.friendsState.copy(isLoading = true)) }
            val friendsResult = repository.fetchFriends()
            val gamificationResult = repository.fetchFriendsGamification()
            if (friendsResult.isSuccess && gamificationResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        friendsState = it.friendsState.copy(
                            isLoading = false,
                            friends = friendsResult.getOrDefault(emptyList()),
                            friendGamification = gamificationResult.getOrDefault(emptyList())
                        )
                    )
                }
            } else {
                _uiState.update { it.copy(friendsState = it.friendsState.copy(isLoading = false)) }
                emitSnackbar(friendsResult.exceptionOrNull()?.message
                    ?: gamificationResult.exceptionOrNull()?.message
                    ?: "Failed to load friends")
            }
        }
    }

    fun addFriend(username: String) {
        viewModelScope.launch {
            repository.addFriend(username)
                .onSuccess { message ->
                    emitSnackbar(message.ifBlank { "Friend added" })
                    refreshFriends()
                }
                .onFailure { error ->
                    emitSnackbar(error.message ?: "Could not add friend")
                }
        }
    }

    fun updateGithub(username: String?, repo: String?, branch: String?, linked: Boolean?) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            repository.updateGithub(username, repo, branch, linked)
                .onSuccess { message ->
                    val updatedProfile = GithubProfile(
                        id = session.user.github?.id ?: "",
                        username = username ?: session.user.github?.username.orEmpty(),
                        repo = repo ?: session.user.github?.repo.orEmpty(),
                        branch = branch ?: session.user.github?.branch.orEmpty(),
                        linked = linked ?: true
                    )
                    val updatedUser = session.user.copy(github = updatedProfile)
                    repository.updateStoredUser(updatedUser)
                    _uiState.update { it.copy(session = Session(session.token, updatedUser)) }
                    emitSnackbar(message.ifBlank { "GitHub profile updated" })
                }
                .onFailure { error ->
                    emitSnackbar(error.message ?: "Could not update GitHub data")
                }
        }
    }

    fun refreshAdmin() {
        val session = _uiState.value.session ?: return
        if (!session.user.isAdmin) return
        viewModelScope.launch {
            _uiState.update { it.copy(adminState = it.adminState.copy(isLoading = true)) }
            repository.fetchAdminUsers()
                .onSuccess { users ->
                    _uiState.update { it.copy(adminState = it.adminState.copy(isLoading = false, users = users)) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(adminState = it.adminState.copy(isLoading = false)) }
                    emitSnackbar(error.message ?: "Failed to load users")
                }
        }
    }

    fun deleteUser(userId: String) {
        val session = _uiState.value.session ?: return
        if (session.user.id == userId) {
            viewModelScope.launch { emitSnackbar("You cannot delete your own account") }
            return
        }
        viewModelScope.launch {
            repository.deleteUser(userId)
                .onSuccess { message ->
                    emitSnackbar(message.ifBlank { "User removed" })
                    refreshAdmin()
                }
                .onFailure { error ->
                    emitSnackbar(error.message ?: "Failed to delete user")
                }
        }
    }

    private fun setAuthLoading(isLoading: Boolean) {
        _uiState.update {
            it.copy(authState = it.authState.copy(isLoading = isLoading, error = null))
        }
    }

    private fun setAuthError(message: String) {
        _uiState.update {
            it.copy(authState = it.authState.copy(isLoading = false, error = message))
        }
        emitSnackbar(message)
    }

    private fun clearData() {
        _uiState.update {
            TraverseUiState(isDarkTheme = it.isDarkTheme)
        }
    }

    private fun refreshAll() {
        viewModelScope.launch { refreshDashboard() }
        viewModelScope.launch { refreshCommits() }
        viewModelScope.launch { refreshProblems() }
        viewModelScope.launch { refreshFriends() }
        if (_uiState.value.session?.user?.isAdmin == true) {
            viewModelScope.launch { refreshAdmin() }
        }
    }

    private fun emitSnackbar(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            _events.emit(UiEvent.SnackbarMessage(message))
        }
    }
}
