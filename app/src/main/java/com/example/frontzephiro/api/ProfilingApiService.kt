// ProfilingApiService.kt
package com.example.frontzephiro.api

import com.example.frontzephiro.models.ProfileCalculationRequest
import com.example.frontzephiro.models.ProfileResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfilingApiService {
    @POST("api/profile/{userId}")
    fun createProfileFromDatabase(
        @Path("userId") userId: String,
        @Query("DataSource") dataSource: String = "Mongo"
    ): Call<Void>
}




