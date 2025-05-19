// Artifact.kt
package com.example.frontzephiro.models

import com.google.gson.annotations.SerializedName

data class Artifact(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("measures")
    val measures: List<String>,

    @SerializedName("periodicity")
    val periodicity: String,

    @SerializedName("questions")
    val questions: List<Question>
)
