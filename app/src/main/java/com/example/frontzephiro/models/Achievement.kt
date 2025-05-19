package com.example.frontzephiro.models

data class Achievement(
    val id: Int,
    val reward: String,
    val title: String, // Este era el antiguo  streakDays
    val description: String,
    val type: String, //daily or one time
    val completed: Boolean
)
