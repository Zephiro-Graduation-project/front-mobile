package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.adapters.SurveyAdapter
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.api.GamificationApiService
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.databinding.ActivitySurveyLargeBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.models.QuestionnaireRequest
import com.example.frontzephiro.models.QuestionnaireResponseDetail
import com.example.frontzephiro.network.RetrofitClient
import com.google.gson.Gson
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
    private lateinit var gamificationService: GamificationApiService

    // Flag para evitar doble envío
    private var isSending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar servicios
        gamificationService = RetrofitClient
            .getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)
        artifactService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)
        questionnaireService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)

        // Título
        binding.tituloEncuesta.text = "PSS"

        // Leer flags
        val readOnly   = intent.getBooleanExtra("READ_ONLY", false)
        val idResponse = intent.getStringExtra("ID_RESPONSE")

        // RecyclerView + Adapter
        surveyAdapter = SurveyAdapter(emptyList(), readOnly)
        binding.rvPreguntas.apply {
            layoutManager = LinearLayoutManager(this@PssActivity)
            setHasFixedSize(true)
            adapter = surveyAdapter
        }

        // Botón enviar
        if (readOnly) {
            binding.botonEnviar.visibility = View.GONE
        } else {
            binding.botonEnviar.setOnClickListener { onSendClicked() }
        }

        // Carga inicial de la encuesta
        loadPssSurvey(readOnly, idResponse)
    }

    private fun onSendClicked() {
        // 1) Evita doble click
        if (isSending) return
        isSending = true
        binding.botonEnviar.apply {
            isEnabled = false
            text = "Enviando..."
        }

        // 2) Validaciones
        val prefs  = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "") ?: ""
        if (userId.isBlank()) {
            Log.e("PssActivity", "No hay usuario autenticado")
            resetSending()
            return
        }
        val responses = surveyAdapter.getResponses()
        if (responses.any { it.selectedAnswer.isBlank() }) {
            Toast.makeText(this, "Por favor, responde todas las preguntas", Toast.LENGTH_SHORT).show()
            resetSending()
            return
        }

        // 3) Preparar y enviar petición
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val request = QuestionnaireRequest(
            userId         = userId,
            surveyId       = "680118ce7b11356ba78f4ec8",
            surveyName     = "Cuestionario de Estrés Percibido (PSS-10)",
            type           = "Psychological",
            completionDate = today,
            responses      = responses
        )

        questionnaireService.addQuestionnaire(request)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                    if (!resp.isSuccessful) {
                        Toast.makeText(this@PssActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        resetSending()
                        return
                    }

                    // Éxito al enviar encuesta
                    Toast.makeText(this@PssActivity, "Encuesta enviada", Toast.LENGTH_SHORT).show()
                    prefs.edit()
                        .putString("PSS_SURVEY_DATE", today)
                        .putString("PSS_ANSWERS", Gson().toJson(request.responses))
                        .apply()

                    // Recompensa por completar encuesta
                    gamificationService.rewardSurvey(userId)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, rewardResp: Response<Void>) {
                                if (rewardResp.isSuccessful) {
                                    Toast.makeText(
                                        this@PssActivity,
                                        "Recompensa por encuesta aplicada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@PssActivity,
                                        "Error recompensa encuesta: ${rewardResp.code()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                // Obtener la racha actual
                                questionnaireService.getStreak(userId)
                                    .enqueue(object : Callback<Int> {
                                        override fun onResponse(call: Call<Int>, streakResp: Response<Int>) {
                                            if (streakResp.isSuccessful) {
                                                val streak = streakResp.body() ?: 0
                                                // Recompensa de racha
                                                gamificationService.rewardStreak(userId, streak)
                                                    .enqueue(object : Callback<Void> {
                                                        override fun onResponse(
                                                            call: Call<Void>,
                                                            rewardStreakResp: Response<Void>
                                                        ) {
                                                            if (rewardStreakResp.isSuccessful) {
                                                                Log.d("PssActivity", "Recompensa de racha aplicada: $streak días")
                                                            } else {
                                                                Log.e("PssActivity", "Error recompensa racha: ${rewardStreakResp.code()}")
                                                            }
                                                            goHome()
                                                        }
                                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                                            Log.e("PssActivity", "Fallo recompensa racha: ${t.message}")
                                                            goHome()
                                                        }
                                                    })
                                            } else {
                                                Log.e("PssActivity", "Error al obtener racha: ${streakResp.code()}")
                                                goHome()
                                            }
                                        }
                                        override fun onFailure(call: Call<Int>, t: Throwable) {
                                            Log.e("PssActivity", "Fallo petición racha: ${t.message}")
                                            goHome()
                                        }
                                    })
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(
                                    this@PssActivity,
                                    "Fallo recompensa encuesta: ${t.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                goHome()
                            }
                        })
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@PssActivity, "Fallo envío encuesta: ${t.message}", Toast.LENGTH_LONG).show()
                    resetSending()
                }
            })
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

    private fun resetSending() {
        isSending = false
        binding.botonEnviar.apply {
            isEnabled = true
            text = "Enviar"
        }
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
