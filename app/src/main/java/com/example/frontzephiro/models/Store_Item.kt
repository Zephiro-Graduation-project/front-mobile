package com.example.frontzephiro.models

data class Store_Item(
    val name: String,
    val price: Int,
    val imageResId: Int, // To do: Ver como se va a recibir la imagen
    val description: String
)
