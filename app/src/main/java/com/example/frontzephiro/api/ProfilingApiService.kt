// ProfilingApiService.kt
package com.example.frontzephiro.api

import com.example.frontzephiro.models.ProfileCalculationRequest
import com.example.frontzephiro.models.ProfileResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfilingApiService {
    @POST("api/profile/{userId}")
    fun createProfileFromDatabase(
        @Path("userId") userId: String,
        @Query("DataSource") dataSource: String = "Mongo"
    ): Call<Void>

    @DELETE("api/profile/{userId}")
    fun deleteProfile(
        @Path("userId") userId: String,
        @Query("dataSource") dataSource: String = "Mongo"
    ): Call<Void>

    @GET("api/Profile/{userId}")
    fun getProfile(
        @Path("userId") userId: String,
        @Query("dataSource") dataSource: String = "Mongo"
    ): Call<ProfileResponse>

    @PUT("api/Profile/{userId}")
    fun updateProfileFromDatabase(
        @Path("userId") userId: String,
        @Query("dataSource") dataSource: String
    ): Call<Void>
}
