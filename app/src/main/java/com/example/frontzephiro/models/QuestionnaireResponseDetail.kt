package com.example.frontzephiro.models

// models/QuestionnaireResponseDetail.kt
data class QuestionnaireResponseDetail(
    val id: String,
    val surveyId: String,
    val surveyName: String,
    val completionDate: String,
    val responses: List<ResponseItem>
)

