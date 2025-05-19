package com.example.frontzephiro.api

import com.example.frontzephiro.models.ContactRequest
import com.example.frontzephiro.models.ContactResponse
import com.example.frontzephiro.models.ContactUpdateRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AlertsApiService {
    @POST("/api/Contacts")
    fun createContact(@Body contact: ContactRequest): Call<ContactResponse>

    @GET("/api/Contacts/{userId}")
    fun getContacts(
        @Path("userId") userId: String,
        @Query("dataSource") dataSource: String = "Mongo"
    ): Call<List<ContactResponse>>

    @DELETE("/api/Contacts/{userId}/{contactId}")
    fun deleteContact(
        @Path("userId") userId: String,
        @Path("contactId") contactId: String
    ): Call<Void>

    @PUT("api/Contacts")
    fun updateContact(@Body contact: ContactUpdateRequest): Call<ContactResponse>
}
