package com.example.frontzephiro.models

data class ContactRequest(
    val userId: String,
    val fullName: String,
    val email: String,
    val cellphone: Long
)
