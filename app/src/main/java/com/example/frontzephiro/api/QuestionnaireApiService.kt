// api/QuestionnaireApiService.kt
package com.example.frontzephiro.api

import com.example.frontzephiro.models.QuestionnaireRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface QuestionnaireApiService {
    @POST("questionnaire/add")
    fun addQuestionnaire(@Body request: QuestionnaireRequest): Call<Void>
}
