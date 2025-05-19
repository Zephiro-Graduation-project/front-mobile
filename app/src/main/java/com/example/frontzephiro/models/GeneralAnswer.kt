// GeneralAnswer.kt
package com.example.frontzephiro.models

import com.google.gson.annotations.SerializedName

data class GeneralAnswer(
    @SerializedName("Responses") val responses: List<ResponseItem>
)
