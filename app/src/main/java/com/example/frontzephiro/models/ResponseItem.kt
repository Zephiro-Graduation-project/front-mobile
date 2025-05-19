// models/ResponseItem.kt
package com.example.frontzephiro.models

data class ResponseItem(
    val question: String,
    var selectedAnswer: String,
    var numericalValue: Int,
    val measures: List<String>
)
