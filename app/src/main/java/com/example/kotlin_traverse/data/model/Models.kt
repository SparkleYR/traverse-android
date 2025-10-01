package com.example.kotlin_traverse.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val token: String = "",
    val user: UserDto = UserDto()
) {
    fun toSession(): Session = Session(token, user.toUser())
}

@Serializable
data class UserDto(
    val id: String? = null,
    @SerialName("_id") val rawId: String? = null,
    val username: String = "",
    val email: String = "",
    val role: String = "student",
    val github: GithubProfileDto = GithubProfileDto()
) {
    fun toUser(): User = User(
        id = id ?: rawId.orEmpty(),
        username = username,
        email = email,
        role = role,
        github = github.toGithubProfile()
    )
}

@Serializable
data class GithubProfileDto(
    val id: String? = null,
    val username: String? = null,
    val repo: String? = null,
    val branch: String? = null,
    val linked: Boolean = false
) {
    fun toGithubProfile(): GithubProfile? =
        if (username.isNullOrBlank() && repo.isNullOrBlank() && branch.isNullOrBlank()) {
            null
        } else {
            GithubProfile(
                id = id.orEmpty(),
                username = username.orEmpty(),
                repo = repo.orEmpty(),
                branch = branch.orEmpty(),
                linked = linked
            )
        }
}

@Serializable
data class GamificationDto(
    @SerialName("user_id") val userId: String? = null,
    val xp: Int = 0,
    @SerialName("streak_days") val streakDays: Int = 0,
    @SerialName("last_streak_date") val lastStreakDate: String? = null,
    val badges: List<String> = emptyList(),
    val level: Int = 1,
    val rank: Int = 0
) {
    fun toGamification(): Gamification = Gamification(
        xp = xp,
        streakDays = streakDays,
        lastStreakDate = lastStreakDate.orEmpty(),
        badges = badges,
        level = level,
        rank = rank
    )
}

@Serializable
data class ProblemSolvedDto(
    val value: Boolean = false,
    val date: Long? = null,
    val tries: Int? = null
)

@Serializable
data class ProblemDto(
    val id: String? = null,
    @SerialName("_id") val rawId: String? = null,
    val name: String = "",
    val platform: String = "",
    val difficulty: Int = 0,
    val solved: ProblemSolvedDto = ProblemSolvedDto(),
    val ignored: Boolean = false,
    @SerialName("parent_topic") val parentTopic: String = "",
    val grandparent: String? = null,
    @SerialName("problem_link") val problemLink: String = "",
    @SerialName("created_at") val createdAt: String? = null
) {
    fun toProblem(): Problem = Problem(
        id = id ?: rawId.orEmpty(),
        name = name,
        platform = platform,
        difficulty = difficulty,
        solved = ProblemSolved(
            value = solved.value,
            date = solved.date,
            tries = solved.tries
        ),
        ignored = ignored,
        parentTopic = parentTopic,
        grandparent = grandparent.orEmpty(),
        problemLink = problemLink
    )
}

@Serializable
data class ProblemsResponseDto(
    val problems: List<ProblemDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10
) {
    fun toProblemsPage(): ProblemsPage = ProblemsPage(
        problems = problems.map { it.toProblem() },
        total = total,
        page = page,
        limit = limit
    )
}

@Serializable
data class FriendDto(
    val id: String? = null,
    @SerialName("_id") val rawId: String? = null,
    val username: String = "",
    val email: String = ""
) {
    fun toFriend(): Friend = Friend(
        id = id ?: rawId.orEmpty(),
        username = username,
        email = email
    )
}

@Serializable
data class FriendGamificationDto(
    val user_id: PopulatedFriendDto = PopulatedFriendDto(),
    val xp: Int = 0,
    @SerialName("streak_days") val streakDays: Int = 0,
    val level: Int = 1,
    val rank: Int = 0
) {
    fun toFriendGamification(): FriendGamification = FriendGamification(
        friend = user_id.toFriend(),
        xp = xp,
        streakDays = streakDays,
        level = level,
        rank = rank
    )
}

@Serializable
data class PopulatedFriendDto(
    val id: String? = null,
    @SerialName("_id") val rawId: String? = null,
    val username: String = ""
) {
    fun toFriend(): Friend = Friend(
        id = id ?: rawId.orEmpty(),
        username = username,
        email = ""
    )
}

@Serializable
data class MessageDto(
    val message: String? = null
)

@Serializable
data class GitCommitDto(
    val sha: String = "",
    val commit: GitCommitInfoDto = GitCommitInfoDto()
) {
    fun toGitCommit(): GitCommit = GitCommit(
        sha = sha,
        message = commit.message,
        author = commit.author.name,
        date = commit.author.date
    )
}

@Serializable
data class GitCommitInfoDto(
    val message: String = "",
    val author: GitCommitAuthorDto = GitCommitAuthorDto()
)

@Serializable
data class GitCommitAuthorDto(
    val name: String = "",
    val email: String = "",
    val date: String = ""
)

// Domain models

data class Session(val token: String, val user: User)

data class User(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val github: GithubProfile?
) {
    val isAdmin: Boolean get() = role.equals("admin", ignoreCase = true)
}

data class GithubProfile(
    val id: String,
    val username: String,
    val repo: String,
    val branch: String,
    val linked: Boolean
)

data class Gamification(
    val xp: Int,
    val streakDays: Int,
    val lastStreakDate: String,
    val badges: List<String>,
    val level: Int,
    val rank: Int
)

data class Problem(
    val id: String,
    val name: String,
    val platform: String,
    val difficulty: Int,
    val solved: ProblemSolved,
    val ignored: Boolean,
    val parentTopic: String,
    val grandparent: String,
    val problemLink: String
)

data class ProblemSolved(
    val value: Boolean,
    val date: Long?,
    val tries: Int?
)

data class ProblemsPage(
    val problems: List<Problem>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class Friend(
    val id: String,
    val username: String,
    val email: String
)

data class FriendGamification(
    val friend: Friend,
    val xp: Int,
    val streakDays: Int,
    val level: Int,
    val rank: Int
)

data class GitCommit(
    val sha: String,
    val message: String,
    val author: String,
    val date: String
)

data class AdminUser(
    val id: String,
    val username: String,
    val email: String,
    val role: String
)

fun UserDto.toAdminUser(): AdminUser = AdminUser(
    id = id ?: rawId.orEmpty(),
    username = username,
    email = email,
    role = role
)
