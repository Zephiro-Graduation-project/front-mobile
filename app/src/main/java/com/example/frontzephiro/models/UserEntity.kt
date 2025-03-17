package com.example.frontzephiro.models

import java.util.Date

data class UserEntity(
    val name: String? = null,
    val mail: String,
    val password: String,
    val birthdate: Date? = null
)
