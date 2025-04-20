// models/QuestionnaireRequest.kt
package com.example.frontzephiro.models

data class QuestionnaireRequest(
    val userId: String,
    val surveyId: String,
    val surveyName: String,
    val type: String,
    val completionDate: String,
    val responses: List<ResponseItem>
)
