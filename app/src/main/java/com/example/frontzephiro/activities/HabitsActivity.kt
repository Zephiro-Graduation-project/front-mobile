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

class HabitsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "HabitsActivity"
    }

    private lateinit var binding: ActivitySurveyLargeBinding
    private lateinit var surveyAdapter: SurveyAdapter
    private lateinit var artifactService: ArtifactApiService
    private lateinit var questionnaireService: QuestionnaireApiService
    private lateinit var gamificationService: GamificationApiService
    private var currentArtifact: Artifact? = null

    // Flag para evitar doble envío
    private var isSending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Servicios
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
        binding.tituloEncuesta.text = "Hábitos"

        // Flags
        val readOnly   = intent.getBooleanExtra("READ_ONLY", false)
        val idResponse = intent.getStringExtra("ID_RESPONSE")

        // RecyclerView + Adapter
        surveyAdapter = SurveyAdapter(emptyList(), readOnly)
        binding.rvPreguntas.apply {
            layoutManager = LinearLayoutManager(this@HabitsActivity)
            setHasFixedSize(true)
            adapter = surveyAdapter
        }

        // Botón enviar
        if (readOnly) {
            binding.botonEnviar.visibility = View.GONE
        } else {
            binding.botonEnviar.setOnClickListener { onSendClicked() }
        }

        // Carga inicial
        loadHabitsSurvey(readOnly, idResponse)
    }

    private fun onSendClicked() {
        // 1) Evito doble click
        if (isSending) return
        isSending = true
        binding.botonEnviar.apply {
            isEnabled = false
            text = "Enviando..."
        }

        // 2) Validaciones
        val artifact = currentArtifact
        if (artifact == null) {
            Toast.makeText(this, "La encuesta aún no ha cargado", Toast.LENGTH_SHORT).show()
            resetSending()
            return
        }
        val prefs  = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "") ?: ""
        if (userId.isBlank()) {
            Log.e(TAG, "No hay usuario autenticado")
            resetSending()
            return
        }
        val responses = surveyAdapter.getResponses()
        if (responses.any { it.selectedAnswer.isBlank() }) {
            Toast.makeText(this, "Por favor, responde todas las preguntas", Toast.LENGTH_SHORT).show()
            resetSending()
            return
        }

        // 3) Preparo petición
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val request = QuestionnaireRequest(
            userId         = userId,
            surveyId       = "680513097959660978ff6e8e",
            surveyName     = artifact.name,
            type           = "Behavioral",
            completionDate = today,
            responses      = responses
        )

        // 4) Envía encuesta
        questionnaireService.addQuestionnaire(request)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                    if (!resp.isSuccessful) {
                        Toast.makeText(this@HabitsActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        resetSending()
                        return
                    }
                    Toast.makeText(this@HabitsActivity, "Encuesta enviada", Toast.LENGTH_SHORT).show()

                    // Guarda en prefs
                    prefs.edit()
                        .putString("HABITS_SURVEY_DATE", today)
                        .putString("HABITS_ANSWERS", Gson().toJson(request.responses))
                        .apply()

                    // Recompensas y fin
                    rewardAndFinish(userId)
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@HabitsActivity, "Fallo envío encuesta: ${t.message}", Toast.LENGTH_LONG).show()
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

    private fun rewardAndFinish(userId: String) {
        // Recompensa por encuesta
        gamificationService.rewardSurvey(userId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, rewardResp: Response<Void>) {
                    if (rewardResp.isSuccessful) {
                        Toast.makeText(
                            this@HabitsActivity,
                            "Recompensa por encuesta aplicada",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@HabitsActivity,
                            "Error recompensa encuesta: ${rewardResp.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // Racha
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
                                                    Log.d(TAG, "Recompensa de racha aplicada: $streak días")
                                                } else {
                                                    Log.e(TAG, "Error recompensa racha: ${rewardStreakResp.code()}")
                                                }
                                                goHome()
                                            }

                                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                                Log.e(TAG, "Fallo recompensa racha: ${t.message}")
                                                goHome()
                                            }
                                        })
                                } else {
                                    Log.e(TAG, "Error al obtener racha: ${streakResp.code()}")
                                    goHome()
                                }
                            }

                            override fun onFailure(call: Call<Int>, t: Throwable) {
                                Log.e(TAG, "Fallo petición racha: ${t.message}")
                                goHome()
                            }
                        })
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@HabitsActivity,
                        "Fallo recompensa encuesta: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    goHome()
                }
            })
    }

    private fun loadHabitsSurvey(readOnly: Boolean, idResponse: String?) {
        artifactService.getHabitsArtifact()
            .enqueue(object : Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val art = resp.body()
                    if (!resp.isSuccessful || art == null) {
                        Toast.makeText(this@HabitsActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    currentArtifact = art
                    surveyAdapter.updateQuestions(art.questions)

                    if (readOnly && idResponse != null) {
                        questionnaireService.getQuestionnaireDetail(idResponse)
                            .enqueue(object : Callback<QuestionnaireResponseDetail> {
                                override fun onResponse(
                                    call: Call<QuestionnaireResponseDetail>,
                                    detailResp: Response<QuestionnaireResponseDetail>
                                ) {
                                    detailResp.body()?.responses?.let {
                                        surveyAdapter.setResponses(it)
                                    }
                                }

                                override fun onFailure(call: Call<QuestionnaireResponseDetail>, t: Throwable) {
                                    Toast.makeText(this@HabitsActivity, "Fallo detalle: ${t.message}", Toast.LENGTH_LONG).show()
                                }
                            })
                    }
                }

                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(this@HabitsActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
