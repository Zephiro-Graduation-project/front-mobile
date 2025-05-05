// ProfileResponse.kt
package com.example.frontzephiro.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ProfileResponse(
    @SerializedName("id")                val id: String,
    @SerializedName("userId")            val userId: String,
    @SerializedName("createdAt")         val createdAt: Date,
    @SerializedName("updatedAt")         val updatedAt: Date? = null,
    @SerializedName("totalStressScore")  val totalStressScore: Int? = null,
    @SerializedName("totalAnxietyScore") val totalAnxietyScore: Int? = null,
    @SerializedName("stressCategory")    val stressCategory: Int? = null,
    @SerializedName("anxietyCategory")   val anxietyCategory: Int? = null,
    @SerializedName("socioDemographicData")
    val socioDemographicData: SocioDemographicData? = null,

    @SerializedName("stressIndicator")
    val stressIndicator: StressIndicator,

    @SerializedName("anxietyIndicator")
    val anxietyIndicator: AnxietyIndicator
)

data class SocioDemographicData(
    @SerializedName("age")                  val age: Int,
    @SerializedName("gender")               val gender: String,
    @SerializedName("socioeconomicLevel")   val socioeconomicLevel: Int,
    @SerializedName("workCondition")        val workCondition: Int,
    @SerializedName("socialSupport")        val socialSupport: Int
)

data class StressIndicator(
    @SerializedName("pssScore")     val pssScore: Int,
    @SerializedName("stressHabits") val stressHabits: StressHabits,
    @SerializedName("totalScore")   val totalScore: Int,
    @SerializedName("category")     val category: String
)

data class StressHabits(
    @SerializedName("relaxationScore")      val relaxationScore: Int,
    @SerializedName("sleepScore")           val sleepScore: Int,
    @SerializedName("physicalScore")        val physicalScore: Int,
    @SerializedName("procrastinationScore") val procrastinationScore: Int,
    @SerializedName("totalScore")           val totalScore: Int
)

data class AnxietyIndicator(
    @SerializedName("gad7Score")     val gad7Score: Int,
    @SerializedName("anxiousHabits") val anxiousHabits: AnxiousHabits,
    @SerializedName("totalScore")    val totalScore: Int,
    @SerializedName("category")      val category: String    // ej. "Leve"
)

data class AnxiousHabits(
    @SerializedName("fearScore")               val fearScore: Int,
    @SerializedName("ruminationScore")         val ruminationScore: Int,
    @SerializedName("repetitiveActionsScore")  val repetitiveActionsScore: Int,
    @SerializedName("hypervigilanceScore")     val hypervigilanceScore: Int,
    @SerializedName("totalScore")              val totalScore: Int
)
