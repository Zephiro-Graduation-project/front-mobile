package com.example.frontzephiro.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.adapters.SurveyAdapter
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.databinding.ActivitySurveySmallPmBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.models.QuestionnaireRequest
import com.example.frontzephiro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class NocturnaActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySurveySmallPmBinding
    private lateinit var surveyAdapter: SurveyAdapter
    private lateinit var questionnaireService: QuestionnaireApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveySmallPmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        surveyAdapter = SurveyAdapter(emptyList())
        binding.rvSurvey.apply {
            layoutManager = LinearLayoutManager(this@NocturnaActivity)
            setHasFixedSize(true)
            adapter = surveyAdapter
        }

        questionnaireService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)

        binding.botonEnviar.setOnClickListener {
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val userId = prefs.getString("USER_ID", "") ?: ""
            if (userId.isBlank()) {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val responses = surveyAdapter.getResponses()
            if (responses.any { it.selectedAnswer.isBlank() }) {
                Toast.makeText(this, "Completa todas las preguntas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date())

            val request = QuestionnaireRequest(
                userId         = userId,
                surveyId       = "NOCTURNO_2025_04",      // o tu lógica dinámica
                surveyName     = "Microsurvey Nocturno",
                type           = "Microsurvey",
                completionDate = today,
                responses      = responses
            )

            questionnaireService.addQuestionnaire(request)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                        if (resp.isSuccessful) {
                            Toast.makeText(this@NocturnaActivity, "Encuesta enviada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@NocturnaActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@NocturnaActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }

        loadNocturnoArtifact()
    }

    private fun loadNocturnoArtifact() {
        RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)
            .getNocturnoArtifact()
            .enqueue(object : Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val art = resp.body()
                    if (!resp.isSuccessful || art == null) {
                        Toast.makeText(this@NocturnaActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    surveyAdapter.updateQuestions(art.questions)
                }
                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(this@NocturnaActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
