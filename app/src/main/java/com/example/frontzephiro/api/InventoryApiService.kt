
package com.example.frontzephiro.api

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Path

interface InventoryApiService {
    @POST("inventory/add/{userId}")
    fun addInventory(@Path("userId") userId: String): Call<Void>
}
