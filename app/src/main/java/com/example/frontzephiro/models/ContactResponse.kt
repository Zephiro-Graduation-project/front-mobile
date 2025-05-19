package com.example.frontzephiro.models

data class ContactResponse(
    val id: String,
    val userId: String,
    val fullName: String,
    val email: String,
    val cellphone: Long,
    val createdAt: String
)
