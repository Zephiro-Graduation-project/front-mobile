// ContentApiService.kt
package com.example.frontzephiro.api

import com.example.frontzephiro.models.Content
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ContentApiService {
    @GET("content/user/all")
    fun getAllContent(): Call<List<Content>>

    @GET("content/user/{id}")
    fun getContentById(@Path("id") id: String): Call<Content>

    @GET("content/user/bytag/{tag_id}")
    fun getContentByTag(@Path("tag_id") tagId: String): Call<List<Content>>
}
