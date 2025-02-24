package com.example.frontzephiro.models

import java.util.Date

data class UserEntity(
    val name: String? = null, //Opcional para login
    val mail: String,
    val password: String,
    val birthdate: Date? = null // Opcional para login
)
