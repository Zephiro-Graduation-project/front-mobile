// Question.kt
package com.example.frontzephiro.models

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("text")
    val text: String,

    @SerializedName("measures")
    val measures: List<String>,

    @SerializedName("answers")
    val answers: List<Answer>
)
