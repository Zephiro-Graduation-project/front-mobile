package com.example.frontzephiro.models

data class StoreProduct(
    val id: Int,
    val name: String,
    val price: Int,
    val imageName: String, // nombre del archivo almacenado en el drawable, para las palantas corresponde al healthyAsset de la planta correspondiente
    val description: String,
    val kind: String // Opciones ("Plant", "Background")
)