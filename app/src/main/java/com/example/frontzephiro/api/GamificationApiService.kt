package com.example.frontzephiro.api

import com.example.frontzephiro.models.Achievement
import com.example.frontzephiro.models.Flower
import com.example.frontzephiro.models.Background
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GamificationApiService {

    @GET("shop/flowers")
    fun getStoreFlowers(): Call<List<Flower>>

    @GET("shop/backgrounds")
    fun getStoreBackgrounds(): Call<List<Background>>

    @GET("shop/achievements")
    fun getAchievements(): Call<List<Achievement>>

    @GET("inventory/{userId}/coins")
    fun getCoins(@Path("userId") userId: String): Call<Int>


    @GET("/inventory/{userId}/flowers")
    fun getInventoryFlowers(@Path("userId") userId: String): Call<List<Flower>>

    @GET("/inventory/{userId}/backgrounds")
    fun getInventoryBackgrounds(@Path("userId") userId: String): Call<List<Background>>
}