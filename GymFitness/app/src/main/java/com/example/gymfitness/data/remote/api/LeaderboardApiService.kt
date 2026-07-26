package com.example.gymfitness.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class LeaderboardEntryDto(
    val userId: String,
    val friendCode: String,
    val displayName: String,
    val avatarInitials: String,
    val weeklyPoints: Int,
    val workoutsThisWeek: Int,
    val currentStreak: Int,
    val steps: Int = 0
)


data class UserProfileRegistration(
    val userId: String,
    val friendCode: String,
    val displayName: String,
    val avatarInitials: String
)

data class AddFriendRequest(
    val userId: String,
    val friendCode: String
)

data class WorkoutPointsDto(
    val userId: String,
    val points: Int,
    val period: String,
    val steps: Int = 0,
    val workoutsCount: Int = 0
)


data class BaseResponse(
    val message: String
)

data class ReferralCodeResponse(
    val friendCode: String
)

interface LeaderboardApiService {

    @POST("api/leaderboard/register")
    suspend fun registerUser(
        @Body profile: UserProfileRegistration
    ): Response<BaseResponse>

    @GET("api/leaderboard/friends/{userId}")
    suspend fun getFriendsLeaderboard(
        @Path("userId") userId: String,
        @retrofit2.http.Query("period") period: String
    ): Response<List<LeaderboardEntryDto>>

    @GET("api/leaderboard/code/{userId}")
    suspend fun getReferralCode(
        @Path("userId") userId: String
    ): Response<ReferralCodeResponse>

    @POST("api/leaderboard/add-friend")
    suspend fun addFriend(
        @Body request: AddFriendRequest
    ): Response<BaseResponse>

    @POST("api/leaderboard/points/update")
    suspend fun updatePoints(
        @Body request: WorkoutPointsDto
    ): Response<BaseResponse>
}

