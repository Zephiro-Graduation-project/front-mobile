package com.example.frontzephiro.models

data class InventoryProduct(
    val name: String,
    val imageName: String, // nombre del archivo almacenado en el drawable, para las palantas corresponde al healthyAsset de la planta correspondiente
    val description: String,
    val kind: String // Opciones ("Plant", "Background")
)