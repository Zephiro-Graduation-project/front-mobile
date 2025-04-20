package com.example.frontzephiro.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.adapters.SurveyAdapter
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.databinding.ActivitySurveySmallBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.models.QuestionnaireRequest
import com.example.frontzephiro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiurnaActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySurveySmallBinding
    private lateinit var surveyAdapter: SurveyAdapter
    private lateinit var questionnaireService: QuestionnaireApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveySmallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Adapter único con lista vacía
        surveyAdapter = SurveyAdapter(emptyList())
        binding.rvSurvey.apply {
            layoutManager = LinearLayoutManager(this@DiurnaActivity)
            setHasFixedSize(true)
            adapter = surveyAdapter
        }

        // 2) Servicio Retrofit para enviar la encuesta
        questionnaireService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)

        // 3) Botón “Enviar”
        binding.botonEnviar.setOnClickListener {
            // --- Aquí recuperas el userId real ---
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val userId = prefs.getString("USER_ID", "") ?: ""
            if (userId.isBlank()) {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtenemos las respuestas del adapter
            val responses = surveyAdapter.getResponses()
            if (responses.any { it.selectedAnswer.isBlank() }) {
                Toast.makeText(this, "Por favor, responde todas las preguntas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Fecha de hoy en yyyy-MM-dd
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Construimos el request usando userId dinámico
            val request = QuestionnaireRequest(
                userId         = userId,
                surveyId       = "DIURNO_2025_04",
                surveyName     = "Microsurvey Diurno",
                type           = "Microsurvey",
                completionDate = today,
                responses      = responses
            )

            // Enviamos la petición
            questionnaireService.addQuestionnaire(request)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                        if (resp.isSuccessful) {
                            Toast.makeText(this@DiurnaActivity, "¡Encuesta enviada!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@DiurnaActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@DiurnaActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }

        // 4) Carga los datos iniciales
        loadDiurnoArtifact()
    }

    private fun loadDiurnoArtifact() {
        RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)
            .getDiurnoArtifact()
            .enqueue(object : Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val artifact = resp.body() ?: run {
                        Toast.makeText(this@DiurnaActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val toShow = artifact.questions.filter { q ->
                        q.measures.any { it in listOf("Stress","Anxiety","Sleep","Control") }
                    }
                    surveyAdapter.updateQuestions(toShow)
                }

                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(this@DiurnaActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}


