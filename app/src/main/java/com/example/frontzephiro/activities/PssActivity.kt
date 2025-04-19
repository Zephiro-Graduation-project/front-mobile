package com.example.frontzephiro.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.databinding.ActivitySurveyLargeBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.network.RetrofitClient
import com.example.frontzephiro.adapters.SurveyAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PssActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveyLargeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) RecyclerView
        binding.rvPreguntas.apply {
            layoutManager = LinearLayoutManager(this@PssActivity)
            setHasFixedSize(true)
        }

        // 2) Botón Enviar
        binding.botonEnviar.setOnClickListener {
            Toast.makeText(this, "Respuestas enviadas", Toast.LENGTH_SHORT).show()
        }

        loadPssSurvey()
    }

    private fun loadPssSurvey() {
        RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)
            .getPssArtifact()
            .enqueue(object : Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, response: Response<Artifact>) {
                    if (!response.isSuccessful || response.body() == null) {
                        Toast.makeText(
                            this@PssActivity,
                            "Error ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    val artifact = response.body()!!
                    // En PSS-10 todas las preguntas son de “Stress”, así que solo pasamos la lista completa
                    binding.rvPreguntas.adapter = SurveyAdapter(artifact.questions)
                }

                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(
                        this@PssActivity,
                        "Fallo: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
