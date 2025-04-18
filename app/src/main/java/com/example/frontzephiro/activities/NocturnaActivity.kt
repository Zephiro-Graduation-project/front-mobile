package com.example.frontzephiro.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.adapters.SurveyAdapter
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.databinding.ActivitySurveySmallPmBinding
import com.example.frontzephiro.databinding.ItemCardAnsiedadBinding
import com.example.frontzephiro.databinding.ItemCardAlimentacionBinding
import com.example.frontzephiro.databinding.ItemCardEstresBinding
import com.example.frontzephiro.databinding.ItemCardEjercicioBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.models.Question
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NocturnaActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySurveySmallPmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveySmallPmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvSurvey.apply {
            layoutManager = LinearLayoutManager(this@NocturnaActivity)
            setHasFixedSize(true)
        }

        binding.botonEnviar.setOnClickListener {
            Toast.makeText(this, "Datos enviados", Toast.LENGTH_SHORT).show()
        }

        RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)
            .getNocturnoArtifact()
            .enqueue(object: Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val art = resp.body()
                    if (!resp.isSuccessful || art == null) {
                        Toast.makeText(this@NocturnaActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    // Muestra todas las preguntas del Nocturno
                    binding.rvSurvey.adapter = SurveyAdapter(art.questions)
                }
                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(this@NocturnaActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
