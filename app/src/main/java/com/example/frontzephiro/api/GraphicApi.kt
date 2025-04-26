package com.example.frontzephiro.api

import com.example.frontzephiro.models.ScoreEntry
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GraphicApi {
    @GET("questionnaire/graphic/{userId}")
    suspend fun getScores(@Path("userId") userId: String): List<ScoreEntry>
}