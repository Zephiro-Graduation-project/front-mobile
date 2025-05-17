package com.example.frontzephiro.api

import com.example.frontzephiro.models.Achievement
import com.example.frontzephiro.models.Flower
import com.example.frontzephiro.models.Background
import com.example.frontzephiro.models.GardenRequest
import com.example.frontzephiro.models.GardenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GamificationApiService {

    @GET("shop/flowers")
    fun getStoreFlowers(): Call<List<Flower>>

    @GET("/shop/flowers/{flowerId}")
    suspend fun getFlowerById(@Path("flowerId") id: Int): Flower

    @GET("shop/backgrounds")
    fun getStoreBackgrounds(): Call<List<Background>>

    @GET("/shop/backgrounds/{backgroundId}")
    suspend fun getBackgroundById(@Path("backgroundId") id: Int): Background

    @GET("shop/achievements")
    fun getAchievements(): Call<List<Achievement>>

    @GET("inventory/{userId}/coins")
    fun getCoins(@Path("userId") userId: String): Call<Int>


    @GET("/inventory/{userId}/flowers")
    fun getInventoryFlowers(@Path("userId") userId: String): Call<List<Flower>>

    @GET("/inventory/{userId}/backgrounds")
    fun getInventoryBackgrounds(@Path("userId") userId: String): Call<List<Background>>

    @PUT("/inventory/{userId}/buyFlower")
    suspend fun buyFlower(@Path("userId") userId: String, @Body flower: Flower): Flower

    @PUT("/inventory/{userId}/buyBackground")
    suspend fun buyBackground(@Path("userId") userId: String, @Body background: Background): Background


    @GET("garden/{userId}")
    fun getUserGarden(@Path("userId") userId: String): Call<GardenResponse>

    @PUT("garden/update/{userId}")
    fun updateGarden(
        @Path("userId") userId: String,
        @Body gardenRequest: GardenRequest
    ): Call<Void>

    @PUT("inventory/{userId}/rewardSurvey")
    fun rewardSurvey(@Path("userId") userId: String): Call<Void>

    @PUT("inventory/{userId}/rewardStreak/{streak}")
    fun rewardStreak(
        @Path("userId") userId: String,
        @Path("streak") streak: Int
    ): Call<Void>

    @PUT("inventory/{userId}/rewardContent")
    fun rewardContent(@Path("userId") userId: String): Call<Void>

    @PUT("inventory/{userId}/rewardEmergencyContact")
    fun rewardEmergencyContact(@Path("userId") userId: String): Call<Void>

}