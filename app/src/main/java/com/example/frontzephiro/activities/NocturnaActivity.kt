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
    private lateinit var gamificationService: GamificationApiService

    // Flag para evitar doble envío
    private var isSending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveySmallPmBinding.inflate(layoutInflater)
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

        // Leer flags
        val readOnly   = intent.getBooleanExtra("READ_ONLY", false)
        val idResponse = intent.getStringExtra("ID_RESPONSE")

        // Configurar RecyclerView
        adapter = SurveyAdapter(emptyList(), readOnly)
        binding.rvSurvey.apply {
            layoutManager = LinearLayoutManager(this@NocturnaActivity)
            setHasFixedSize(true)
            adapter = this@NocturnaActivity.adapter
        }

        // Botón enviar
        if (readOnly) {
            binding.botonEnviar.visibility = View.GONE
        } else {
            binding.botonEnviar.setOnClickListener { onSendClicked() }
        }

        // Cargar preguntas
        loadNocturnoArtifact(readOnly, idResponse)
    }

    private fun onSendClicked() {
        // 1) Evita doble click
        if (isSending) return
        isSending = true
        binding.botonEnviar.apply {
            isEnabled = false
            text = "Enviando..."
        }

        // 2) Validaciones previas
        val prefs  = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "") ?: ""
        if (userId.isBlank()) {
            Log.e("NocturnaActivity", "Usuario no autenticado")
            resetSending()
            return
        }
        val responses = adapter.getResponses()
        if (responses.any { it.selectedAnswer.isBlank() }) {
            Toast.makeText(this, "Completa todas las preguntas", Toast.LENGTH_SHORT).show()
            resetSending()
            return
        }

        // 3) Preparo petición
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val req = QuestionnaireRequest(
            userId         = userId,
            surveyId       = "NOCTURNO_2025_04",
            surveyName     = "Microsurvey Nocturno",
            type           = "Microsurvey",
            completionDate = today,
            responses      = responses
        )

        // 4) Envío
        questionnaireService.addQuestionnaire(req)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                    if (!resp.isSuccessful) {
                        Toast.makeText(this@NocturnaActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        resetSending()
                        return
                    }

                    // Éxito: guardo fecha y notifico
                    prefs.edit()
                        .putString("NOCTURNO_SURVEY_DATE", today)
                        .apply()
                    Toast.makeText(this@NocturnaActivity, "¡Encuesta enviada!", Toast.LENGTH_SHORT).show()

                    // Recompensa por encuesta
                    gamificationService.rewardSurvey(userId)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, rewardResp: Response<Void>) {
                                if (rewardResp.isSuccessful) {
                                    Toast.makeText(
                                        this@NocturnaActivity,
                                        "Recompensa por encuesta aplicada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@NocturnaActivity,
                                        "Error recompensa encuesta: ${rewardResp.code()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                // Obtener y premiar racha
                                questionnaireService.getStreak(userId)
                                    .enqueue(object : Callback<Int> {
                                        override fun onResponse(call: Call<Int>, streakResp: Response<Int>) {
                                            if (streakResp.isSuccessful) {
                                                val streak = streakResp.body() ?: 0
                                                gamificationService.rewardStreak(userId, streak)
                                                    .enqueue(object : Callback<Void> {
                                                        override fun onResponse(
                                                            call: Call<Void>,
                                                            rewardStreakResp: Response<Void>
                                                        ) {
                                                            if (rewardStreakResp.isSuccessful) {
                                                                Log.d("NocturnaActivity", "Recompensa de racha aplicada: $streak días")
                                                            } else {
                                                                Log.e("NocturnaActivity", "Error recompensa racha: ${rewardStreakResp.code()}")
                                                            }
                                                            goHome()
                                                        }
                                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                                            Log.e("NocturnaActivity", "Fallo recompensa racha: ${t.message}")
                                                            goHome()
                                                        }
                                                    })
                                            } else {
                                                Log.e("NocturnaActivity", "Error al obtener racha: ${streakResp.code()}")
                                                goHome()
                                            }
                                        }
                                        override fun onFailure(call: Call<Int>, t: Throwable) {
                                            Log.e("NocturnaActivity", "Fallo petición racha: ${t.message}")
                                            goHome()
                                        }
                                    })
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(
                                    this@NocturnaActivity,
                                    "Fallo recompensa encuesta: ${t.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                goHome()
                            }
                        })
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@NocturnaActivity, "Fallo envío encuesta: ${t.message}", Toast.LENGTH_LONG).show()
                    resetSending()
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

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
