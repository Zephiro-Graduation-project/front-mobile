package com.example.frontzephiro.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.adapters.SurveyAdapter
import com.example.frontzephiro.databinding.ActivitySurveySmallBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DiurnaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveySmallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveySmallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Configurar RecyclerView
        binding.rvSurvey.apply {
            layoutManager = LinearLayoutManager(this@DiurnaActivity)
            setHasFixedSize(true)
        }

        // 2) Botón “Enviar”
        binding.botonEnviar.setOnClickListener {
            Toast.makeText(this, "Datos enviados", Toast.LENGTH_SHORT).show()
        }

        // 3) Cargar datos
        loadDiurnoArtifact()
    }

    private fun loadDiurnoArtifact() {
        RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)
            .getDiurnoArtifact()
            .enqueue(object : Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val artifact = resp.body()
                    if (!resp.isSuccessful || artifact == null) {
                        Toast.makeText(
                            this@DiurnaActivity,
                            "Error ${resp.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    // Filtramos solo las medidas que nos interesan
                    val toShow = artifact.questions.filter { question ->
                        question.measures.any { it in listOf("Stress","Anxiety","Sleep","Control") }
                    }

                    // 4) Poner el adapter
                    binding.rvSurvey.adapter = SurveyAdapter(toShow)
                }

                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(
                        this@DiurnaActivity,
                        "Fallo: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
