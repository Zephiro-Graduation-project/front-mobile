// ProfilingApiService.kt
package com.example.frontzephiro.api

import com.example.frontzephiro.models.ProfileResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProfilingApiService {
    @GET("api/profile/{userId}")
    fun getProfile(@Path("userId") userId: String): Call<ProfileResponse>
}
