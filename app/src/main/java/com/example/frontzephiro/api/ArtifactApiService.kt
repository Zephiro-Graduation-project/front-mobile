package com.example.frontzephiro.api

import com.example.frontzephiro.models.Artifact
import retrofit2.Call
import retrofit2.http.GET

interface ArtifactApiService {
    @GET("artifact/diurno")
    fun getDiurnoArtifact(): Call<Artifact>

    @GET("artifact/nocturno")
    fun getNocturnoArtifact(): Call<Artifact>

    @GET("artifact/pss")
    fun getPssArtifact(): Call<Artifact>

    @GET("artifact/gad")
    fun getGadArtifact(): Call<Artifact>

    @GET("artifact/hábitos")
    fun getHabitsArtifact(): Call<Artifact>

    @GET("artifact/sociodemográfico")
    fun getDemographicsArtifact(): Call<Artifact>
}

