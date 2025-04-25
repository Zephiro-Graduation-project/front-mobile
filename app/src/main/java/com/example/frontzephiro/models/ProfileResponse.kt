// ProfileResponse.kt
package com.example.frontzephiro.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ProfileResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("createdAt") val createdAt: Date,
    @SerializedName("updatedAt") val updatedAt: Date,
    @SerializedName("totalStressScore") val totalStressScore: Int,
    @SerializedName("totalAnxietyScore") val totalAnxietyScore: Int,
    @SerializedName("stressCategory") val stressCategory: Int,
    @SerializedName("anxietyCategory") val anxietyCategory: Int,
    @SerializedName("socioDemographicData") val socioDemographicData: SocioDemographicData,
    @SerializedName("stressIndicator") val stressIndicator: StressIndicator,
    @SerializedName("anxietyIndicator") val anxietyIndicator: AnxietyIndicator
)

data class SocioDemographicData(
    val age: Int,
    val gender: String,
    val socioeconomicLevel: Int,
    val workCondition: Int,
    val socialSupport: Int
)

data class StressIndicator(
    val pssScore: Int,
    val generalHabits: GeneralHabits,
    val totalScore: Int,
    val category: Int
)

data class GeneralHabits(
    val alimentationScore: Int,
    val sleepScore: Int,
    val physicalScore: Int,
    val procrastinationScore: Int,
    val totalScore: Int
)

data class AnxietyIndicator(
    val gad7Score: Int,
    val anxiousHabits: AnxiousHabits,
    val totalScore: Int,
    val category: Int
)

data class AnxiousHabits(
    val deviceUseScore: Int,
    val ruminationScore: Int,
    val repetitiveActionsScore: Int,
    val hypervigilanceScore: Int,
    val totalScore: Int
)
