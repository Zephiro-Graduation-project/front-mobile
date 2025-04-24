package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.adapters.DemographicsAdapter
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.databinding.ActivitySurveyLargeDemographicsBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.models.QuestionnaireRequest
import com.example.frontzephiro.models.QuestionnaireResponseDetail
import com.example.frontzephiro.models.ResponseItem
import com.example.frontzephiro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DemographicsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySurveyLargeDemographicsBinding
    private lateinit var adapter: DemographicsAdapter
    private lateinit var artifactService: ArtifactApiService
    private lateinit var questionnaireService: QuestionnaireApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeDemographicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val readOnly   = intent.getBooleanExtra("READ_ONLY", false)
        val idResponse = intent.getStringExtra("ID_RESPONSE")

        adapter = DemographicsAdapter(emptyList(), readOnly)
        binding.rvPreguntas.apply {
            layoutManager = LinearLayoutManager(this@DemographicsActivity)
            setHasFixedSize(true)
            adapter = this@DemographicsActivity.adapter
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
                val prefs  = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                val userId = prefs.getString("USER_ID", "") ?: ""
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
                ).apply { addAll(rest) }
                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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
                                Toast.makeText(this@DemographicsActivity, "Encuesta enviada", Toast.LENGTH_SHORT).show()
                                goHabits()
                            } else {
                                Toast.makeText(this@DemographicsActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@DemographicsActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }

        loadDemographics(readOnly, idResponse)
    }

    private fun loadDemographics(readOnly: Boolean, idResponse: String?) {
        artifactService.getDemographicsArtifact()
            .enqueue(object : Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val art = resp.body()
                    if (!resp.isSuccessful || art == null) {
                        Toast.makeText(
                            this@DemographicsActivity,
                            "Error ${resp.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    if (art.questions.isNotEmpty()) {
                        binding.preguntaEdad.text = art.questions[0].text
                    }
                    val restQs = if (art.questions.size > 1) art.questions.drop(1) else emptyList()
                    adapter.updateQuestions(restQs)

                    if (readOnly && idResponse != null) {
                        questionnaireService.getQuestionnaireDetail(idResponse)
                            .enqueue(object : Callback<QuestionnaireResponseDetail> {
                                override fun onResponse(
                                    call: Call<QuestionnaireResponseDetail>,
                                    resp: Response<QuestionnaireResponseDetail>
                                ) {
                                    val detail = resp.body()
                                    if (!resp.isSuccessful || detail == null) {
                                        Toast.makeText(
                                            this@DemographicsActivity,
                                            "Error detalle ${resp.code()}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return
                                    }
                                    detail.responses
                                        .find { it.question == binding.preguntaEdad.text.toString() }
                                        ?.let {
                                            binding.etEdad.setText(it.selectedAnswer)
                                            binding.etEdad.isEnabled = false
                                        }
                                    val restResps = detail.responses.filter {
                                        it.question != binding.preguntaEdad.text.toString()
                                    }
                                    adapter.setResponses(restResps)
                                }
                                override fun onFailure(call: Call<QuestionnaireResponseDetail>, t: Throwable) {
                                    Toast.makeText(
                                        this@DemographicsActivity,
                                        "Fallo detalle: ${t.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            })
                    }
                }
                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(
                        this@DemographicsActivity,
                        "Fallo de red: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun goHabits() {
        startActivity(Intent(this, HabitsActivity::class.java))
        finish()
    }
}
