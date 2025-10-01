package com.example.kotlin_traverse.data.remote

import com.example.kotlin_traverse.data.model.AuthResponseDto
import com.example.kotlin_traverse.data.model.FriendDto
import com.example.kotlin_traverse.data.model.FriendGamificationDto
import com.example.kotlin_traverse.data.model.GamificationDto
import com.example.kotlin_traverse.data.model.GitCommitDto
import com.example.kotlin_traverse.data.model.MessageDto
import com.example.kotlin_traverse.data.model.ProblemsResponseDto
import com.example.kotlin_traverse.data.model.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TraverseApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponseDto

    @PUT("auth/github")
    suspend fun updateGithub(@Body body: UpdateGithubRequest): MessageDto

    @GET("problems")
    suspend fun getProblems(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): ProblemsResponseDto

    @GET("problems/solved")
    suspend fun getSolvedProblems(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): ProblemsResponseDto

    @POST("problems/push")
    suspend fun pushProblem(@Body body: PushProblemRequest): MessageDto

    @GET("gamification")
    suspend fun getGamification(): GamificationDto

    @GET("friends")
    suspend fun getFriends(): List<FriendDto>

    @POST("friends/add")
    suspend fun addFriend(@Body body: AddFriendRequest): MessageDto

    @GET("friends/gamification")
    suspend fun getFriendsGamification(): List<FriendGamificationDto>

    @GET("git/getGitSolvedQuestions")
    suspend fun getGitCommits(): List<GitCommitDto>

    @GET("admin/users")
    suspend fun getUsers(): List<UserDto>

    @DELETE("admin/users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String): MessageDto
}
