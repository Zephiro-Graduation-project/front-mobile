// ProfileCalculationRequest.kt
package com.example.frontzephiro.models

import com.google.gson.annotations.SerializedName

data class ProfileCalculationRequest(
    @SerializedName("UserId")               val userId: String,
    @SerializedName("DataSource")           val dataSource: String = "Mongo",
    @SerializedName("Gad7Answers")          val gad7Answers: GeneralAnswer,
    @SerializedName("PssAnswers")           val pssAnswers: GeneralAnswer,
    @SerializedName("SocioDemographicData") val socioDemographicData: GeneralAnswer
)

