package com.example.frontzephiro.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Flower(
    var id: Int,
    var name: String,
    val description: String,
    var price: Int,
    var healthyAsset: String,
    var dryAsset: String
) : Parcelable
