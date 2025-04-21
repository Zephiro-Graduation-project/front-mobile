package com.example.frontzephiro.api

import com.example.frontzephiro.models.Flower
import com.example.frontzephiro.models.Background
import retrofit2.Call
import retrofit2.http.GET

interface GamificationApiService {

    @GET("shop/flowers")
    fun getStoreFlowers(): Call<List<Flower>>

    @GET("shop/backgrounds")
    fun getStoreBackgrounds(): Call<List<Background>>
}