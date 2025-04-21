// NocturnaActivity.kt
package com.example.frontzephiro.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.adapters.SurveyAdapter
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.databinding.ActivitySurveySmallPmBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.models.QuestionnaireRequest
import com.example.frontzephiro.models.QuestionnaireResponseDetail
import com.example.frontzephiro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class NocturnaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveySmallPmBinding
    private lateinit var adapter: SurveyAdapter

    private lateinit var artifactService: ArtifactApiService
    private lateinit var questionnaireService: QuestionnaireApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveySmallPmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val readOnly   = intent.getBooleanExtra("READ_ONLY", false)
        val idResponse = intent.getStringExtra("ID_RESPONSE")

        // Adapter
        adapter = SurveyAdapter(emptyList(), readOnly)
        binding.rvSurvey.apply {
            layoutManager = LinearLayoutManager(this@NocturnaActivity)
            setHasFixedSize(true)
            adapter = this@NocturnaActivity.adapter
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
                val responses = adapter.getResponses()
                if (responses.any { it.selectedAnswer.isBlank() }) {
                    Toast.makeText(this, "Completa todas las preguntas", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val req = QuestionnaireRequest(
                    userId         = userId,
                    surveyId       = "NOCTURNO_2025_04",
                    surveyName     = "Microsurvey Nocturno",
                    type           = "Microsurvey",
                    completionDate = today,
                    responses      = responses
                )
                questionnaireService.addQuestionnaire(req)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                            Toast.makeText(
                                this@NocturnaActivity,
                                if (resp.isSuccessful) "Encuesta enviada"
                                else "Error ${resp.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@NocturnaActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }

        loadNocturnoArtifact(readOnly, idResponse)
    }

    private fun loadNocturnoArtifact(readOnly: Boolean, idResponse: String?) {
        artifactService.getNocturnoArtifact()
            .enqueue(object : Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val art = resp.body()
                    if (!resp.isSuccessful || art == null) {
                        Toast.makeText(this@NocturnaActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    adapter.updateQuestions(art.questions)

                    if (readOnly && idResponse != null) {
                        questionnaireService.getQuestionnaireDetail(idResponse)
                            .enqueue(object : Callback<QuestionnaireResponseDetail> {
                                override fun onResponse(
                                    call: Call<QuestionnaireResponseDetail>,
                                    resp: Response<QuestionnaireResponseDetail>
                                ) {
                                    resp.body()?.responses?.let { adapter.setResponses(it) }
                                }
                                override fun onFailure(call: Call<QuestionnaireResponseDetail>, t: Throwable) {
                                    Toast.makeText(this@NocturnaActivity, "Fallo detalle: ${t.message}", Toast.LENGTH_LONG).show()
                                }
                            })
                    }
                }
                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(this@NocturnaActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
