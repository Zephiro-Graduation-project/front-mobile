package com.example.frontzephiro.models

data class GardenResponse(
    val userId: String,
    val background: Background,
    val id: String,
    val state: Boolean,
    val flowers: List<Flower?> // lista de 12 elementos, algunos null
)
