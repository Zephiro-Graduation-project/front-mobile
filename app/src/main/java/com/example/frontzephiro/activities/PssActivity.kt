// PssActivity.kt
package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.adapters.SurveyAdapter
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.databinding.ActivitySurveyLargeBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.models.QuestionnaireRequest
import com.example.frontzephiro.models.QuestionnaireResponseDetail
import com.example.frontzephiro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PssActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveyLargeBinding
    private lateinit var surveyAdapter: SurveyAdapter
    private lateinit var artifactService: ArtifactApiService
    private lateinit var questionnaireService: QuestionnaireApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val readOnly   = intent.getBooleanExtra("READ_ONLY", false)
        val idResponse = intent.getStringExtra("ID_RESPONSE")

        surveyAdapter = SurveyAdapter(emptyList(), readOnly)
        binding.rvPreguntas.apply {
            layoutManager = LinearLayoutManager(this@PssActivity)
            setHasFixedSize(true)
            adapter = surveyAdapter
        }

        artifactService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)
        questionnaireService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)

        if (readOnly) {
            binding.botonEnviar.visibility = View.GONE
        } else {
            binding.botonEnviar.setOnClickListener {
                val userId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                    .getString("USER_ID", "") ?: ""
                if (userId.isBlank()) {
                    Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val responses = surveyAdapter.getResponses()
                if (responses.any { it.selectedAnswer.isBlank() }) {
                    Toast.makeText(this, "Por favor, responde todas las preguntas", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val request = QuestionnaireRequest(
                    userId         = userId,
                    surveyId       = "PSS10_2025_04",
                    surveyName     = "Cuestionario de Estrés Percibido (PSS‑10)",
                    type           = "Psychological",
                    completionDate = today,
                    responses      = responses
                )
                questionnaireService.addQuestionnaire(request)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                            if (resp.isSuccessful) {
                                Toast.makeText(this@PssActivity, "Encuesta enviada", Toast.LENGTH_SHORT).show()
                                goGad()
                            } else {
                                Toast.makeText(this@PssActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@PssActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }

        loadPssSurvey(readOnly, idResponse)
    }

    private fun loadPssSurvey(readOnly: Boolean, idResponse: String?) {
        artifactService.getPssArtifact()
            .enqueue(object : Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val art = resp.body()
                    if (!resp.isSuccessful || art == null) {
                        Toast.makeText(this@PssActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    surveyAdapter.updateQuestions(art.questions)

                    if (readOnly && idResponse != null) {
                        questionnaireService.getQuestionnaireDetail(idResponse)
                            .enqueue(object : Callback<QuestionnaireResponseDetail> {
                                override fun onResponse(
                                    call: Call<QuestionnaireResponseDetail>,
                                    resp: Response<QuestionnaireResponseDetail>
                                ) {
                                    resp.body()?.responses?.let { surveyAdapter.setResponses(it) }
                                }
                                override fun onFailure(call: Call<QuestionnaireResponseDetail>, t: Throwable) {
                                    Toast.makeText(this@PssActivity, "Fallo detalle: ${t.message}", Toast.LENGTH_LONG).show()
                                }
                            })
                    }
                }
                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(this@PssActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun goGad() {
        startActivity(Intent(this, GadActivity::class.java))
        finish()
    }
}
