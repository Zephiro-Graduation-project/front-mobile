package com.example.frontzephiro.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.adapters.DemographicsAdapter
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.databinding.ActivitySurveyLargeDemographicsBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.models.QuestionnaireRequest
import com.example.frontzephiro.models.ResponseItem
import com.example.frontzephiro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DemographicsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySurveyLargeDemographicsBinding
    private lateinit var adapter: DemographicsAdapter
    private lateinit var questionnaireService: QuestionnaireApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeDemographicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Adapter inicial vacío
        adapter = DemographicsAdapter(emptyList())
        binding.rvPreguntas.apply {
            layoutManager = LinearLayoutManager(this@DemographicsActivity)
            adapter = this@DemographicsActivity.adapter
        }

        questionnaireService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)

        binding.botonEnviar.setOnClickListener {
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val userId = prefs.getString("USER_ID","") ?: ""
            if (userId.isBlank()) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val edadStr = binding.etEdad.text.toString().trim()
            if (edadStr.isBlank()) {
                Toast.makeText(this, "Por favor ingresa tu edad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rest = adapter.getResponses()
            if (rest.any { it.selectedAnswer.isBlank() }) {
                Toast.makeText(this, "Completa todas las preguntas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val responses = mutableListOf<ResponseItem>(
                ResponseItem(
                    question       = binding.preguntaEdad.text.toString(),
                    selectedAnswer = edadStr,
                    numericalValue = edadStr.toIntOrNull() ?: 0,
                    measures       = listOf("Demographics")
                )
            )
            responses.addAll(rest)

            val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date())

            val request = QuestionnaireRequest(
                userId         = userId,
                surveyId       = "DEMOG_2025_04",
                surveyName     = "Cuestionario Sociodemográfico",
                type           = "Demographics",
                completionDate = fecha,
                responses      = responses
            )

            questionnaireService.addQuestionnaire(request)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                        if (resp.isSuccessful) {
                            Toast.makeText(this@DemographicsActivity,
                                "Encuesta enviada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@DemographicsActivity,
                                "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@DemographicsActivity,
                            "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }

        loadDemographicsSurvey()
    }

    private fun loadDemographicsSurvey() {
        RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)
            .getDemographicsArtifact()
            .enqueue(object : Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val art = resp.body()
                    if (!resp.isSuccessful || art == null) {
                        Toast.makeText(this@DemographicsActivity,
                            "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val rest = if (art.questions.size > 1) art.questions.drop(1) else emptyList()
                    adapter.updateQuestions(rest)
                    if (art.questions.isNotEmpty())
                        binding.preguntaEdad.text = art.questions[0].text
                }
                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(this@DemographicsActivity,
                        "Fallo de red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
