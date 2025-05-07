package com.example.frontzephiro.api

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Path

interface GardenApiService {
    @POST("garden/add/{userId}")
    fun addGarden(@Path("userId") userId: String): Call<Void>
}