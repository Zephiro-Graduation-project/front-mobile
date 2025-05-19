package com.example.frontzephiro.models

import com.google.gson.annotations.SerializedName

data class ContactUpdateRequest(
    @SerializedName("contactId")
    val contactId: String,    // aquí va el ID del contacto a editar
    @SerializedName("userId")
    val userId: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("cellphone")
    val cellphone: Long
)
