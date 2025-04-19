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

class GadActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveyLargeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView dentro de ScrollView — no asumimos tamaño fijo ni nested scroll
        binding.rvPreguntas.apply {
            layoutManager = LinearLayoutManager(this@GadActivity)
            setHasFixedSize(false)              // <- importante
            isNestedScrollingEnabled = false    // <- desactivar su scroll interno
        }

        binding.botonEnviar.setOnClickListener {
            Toast.makeText(this, "Respuestas enviadas", Toast.LENGTH_SHORT).show()
        }

        loadGadSurvey()
    }


    private fun loadGadSurvey() {
        val service = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)

        service.getGadArtifact().enqueue(object : Callback<Artifact> {
            override fun onResponse(call: Call<Artifact>, response: Response<Artifact>) {
                if (!response.isSuccessful || response.body() == null) {
                    Toast.makeText(
                        this@GadActivity,
                        "Error cargando GAD‑7: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                val artifact = response.body()!!
                // Todas las preguntas son de “Anxiety”, así que las pasamos directamente
                binding.rvPreguntas.adapter = SurveyAdapter(artifact.questions)
            }

            override fun onFailure(call: Call<Artifact>, t: Throwable) {
                Toast.makeText(
                    this@GadActivity,
                    "Fallo en la petición: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
