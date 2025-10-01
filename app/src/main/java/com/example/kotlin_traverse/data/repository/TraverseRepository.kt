package com.example.kotlin_traverse.data.repository

import com.example.kotlin_traverse.data.local.TokenStore
import com.example.kotlin_traverse.data.model.AdminUser
import com.example.kotlin_traverse.data.model.AuthResponseDto
import com.example.kotlin_traverse.data.model.Friend
import com.example.kotlin_traverse.data.model.FriendGamification
import com.example.kotlin_traverse.data.model.Gamification
import com.example.kotlin_traverse.data.model.GitCommit
import com.example.kotlin_traverse.data.model.Problem
import com.example.kotlin_traverse.data.model.ProblemsPage
import com.example.kotlin_traverse.data.model.Session
import com.example.kotlin_traverse.data.model.User
import com.example.kotlin_traverse.data.model.toAdminUser
import com.example.kotlin_traverse.data.remote.AddFriendRequest
import com.example.kotlin_traverse.data.remote.LoginRequest
import com.example.kotlin_traverse.data.remote.PushProblemRequest
import com.example.kotlin_traverse.data.remote.RegisterRequest
import com.example.kotlin_traverse.data.remote.TraverseApi
import com.example.kotlin_traverse.data.remote.UpdateGithubRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TraverseRepository(
    private val api: TraverseApi,
    private val tokenStore: TokenStore
) {

    suspend fun register(username: String, email: String, password: String, githubUsername: String?, githubRepo: String?, githubBranch: String?): Result<Session> =
        runApiCall {
            api.register(
                RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    githubUsername = githubUsername?.ifBlank { null },
                    githubRepo = githubRepo?.ifBlank { null },
                    githubBranch = githubBranch?.ifBlank { null }
                )
            )
        }

    suspend fun login(email: String, password: String): Result<Session> =
        runApiCall { api.login(LoginRequest(email = email, password = password)) }

    suspend fun updateGithub(username: String?, repo: String?, branch: String?, linked: Boolean?): Result<String> =
        runCatching {
            val response = api.updateGithub(
                UpdateGithubRequest(
                    github = UpdateGithubRequest.GithubUpdate(
                        username = username?.ifBlank { null },
                        repo = repo?.ifBlank { null },
                        branch = branch?.ifBlank { null },
                        linked = linked
                    )
                )
            )
            response.message.orEmpty()
        }

    suspend fun fetchProblems(page: Int, limit: Int): Result<ProblemsPage> =
        runCatching { api.getProblems(page, limit).toProblemsPage() }

    suspend fun fetchSolvedProblems(page: Int, limit: Int): Result<ProblemsPage> =
        runCatching { api.getSolvedProblems(page, limit).toProblemsPage() }

    suspend fun pushProblem(request: PushProblemRequest): Result<String> =
        runCatching { api.pushProblem(request).message.orEmpty() }

    suspend fun fetchGamification(): Result<Gamification> =
        runCatching { api.getGamification().toGamification() }

    suspend fun fetchFriends(): Result<List<Friend>> =
        runCatching { api.getFriends().map { it.toFriend() } }

    suspend fun addFriend(username: String): Result<String> =
        runCatching { api.addFriend(AddFriendRequest(username)).message.orEmpty() }

    suspend fun fetchFriendsGamification(): Result<List<FriendGamification>> =
        runCatching { api.getFriendsGamification().map { it.toFriendGamification() } }

    suspend fun fetchGitCommits(): Result<List<GitCommit>> =
        runCatching { api.getGitCommits().map { it.toGitCommit() } }

    suspend fun fetchAdminUsers(): Result<List<AdminUser>> =
        runCatching { api.getUsers().map { it.toAdminUser() } }

    suspend fun deleteUser(userId: String): Result<String> =
        runCatching { api.deleteUser(userId).message.orEmpty() }

    suspend fun persistSession(session: Session) {
        withContext(Dispatchers.IO) { tokenStore.saveSession(session) }
    }

    suspend fun clearSession() {
        withContext(Dispatchers.IO) { tokenStore.clearSession() }
    }

    suspend fun updateStoredUser(user: User) {
        withContext(Dispatchers.IO) { tokenStore.updateSessionUser(user) }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        withContext(Dispatchers.IO) { tokenStore.setDarkTheme(isDark) }
    }

    private suspend fun runApiCall(block: suspend () -> AuthResponseDto): Result<Session> =
        runCatching { block().toSession() }
}
