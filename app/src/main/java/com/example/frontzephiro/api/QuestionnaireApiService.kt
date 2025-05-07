// api/QuestionnaireApiService.kt
package com.example.frontzephiro.api

import com.example.frontzephiro.models.QuestionnaireRequest
import com.example.frontzephiro.models.QuestionnaireSummary
import com.example.frontzephiro.models.QuestionnaireResponseDetail
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface QuestionnaireApiService {
    @POST("questionnaire/add")
    fun addQuestionnaire(@Body request: QuestionnaireRequest): Call<Void>

    @GET("questionnaire/dates/{userId}")
    fun getQuestionnaireDates(@Path("userId") userId: String): Call<List<String>>

    @GET("questionnaire/ondate/{userId}/{date}")
    fun getQuestionnairesOnDate(
        @Path("userId") userId: String,
        @Path("date")   date:   String
    ): Call<List<QuestionnaireSummary>>

    @GET("questionnaire/specific/{idResponse}")
    fun getQuestionnaireDetail(
        @Path("idResponse") idResponse: String
    ): Call<QuestionnaireResponseDetail>
}
