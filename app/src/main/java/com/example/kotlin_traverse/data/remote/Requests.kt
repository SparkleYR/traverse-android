package com.example.kotlin_traverse.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerialName("github_username") val githubUsername: String? = null,
    @SerialName("github_repo") val githubRepo: String? = null,
    @SerialName("github_branch") val githubBranch: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UpdateGithubRequest(
    val github: GithubUpdate
) {
    @Serializable
    data class GithubUpdate(
        val username: String? = null,
        val repo: String? = null,
        val branch: String? = null,
        val linked: Boolean? = null
    )
}

@Serializable
data class PushProblemRequest(
    val name: String,
    val platform: String,
    val difficulty: Int,
    val solved: ProblemSolvedPayload = ProblemSolvedPayload(),
    val ignored: Boolean = false,
    @SerialName("parent_topic") val parentTopic: String,
    val grandparent: String? = null,
    @SerialName("problem_link") val problemLink: String
)

@Serializable
data class ProblemSolvedPayload(
    val value: Boolean = false,
    val date: Long? = null,
    val tries: Int? = null
)

@Serializable
data class AddFriendRequest(
    val username: String
)
