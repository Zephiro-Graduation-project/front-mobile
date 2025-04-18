// Answer.kt
package com.example.frontzephiro.models

import com.google.gson.annotations.SerializedName

data class Answer(
    @SerializedName("text")
    val text: String,

    @SerializedName("numericValue")
    val numericValue: Int
)
