package com.example.frontzephiro.api

import com.example.frontzephiro.models.Tag
import retrofit2.Call
import retrofit2.http.GET

interface TagsApiService {
    @GET("content/admin/tags/all")
    fun getAllTags(): Call<List<Tag>>
}
