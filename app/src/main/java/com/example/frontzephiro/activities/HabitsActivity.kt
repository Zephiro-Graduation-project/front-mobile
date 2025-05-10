// HabitsActivity.kt
package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson

class HabitsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveyLargeBinding
    private lateinit var surveyAdapter: SurveyAdapter
    private lateinit var artifactService: ArtifactApiService
    private lateinit var questionnaireService: QuestionnaireApiService
    private var currentArtifact: Artifact? = null
    private lateinit var gamificationService: GamificationApiService

    override fun onCreate(savedInstanceState: Bundle?) {

        gamificationService = RetrofitClient
            .getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)

        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val readOnly   = intent.getBooleanExtra("READ_ONLY", false)
        val idResponse = intent.getStringExtra("ID_RESPONSE")

        surveyAdapter = SurveyAdapter(emptyList(), readOnly)
        binding.rvPreguntas.apply {
            layoutManager = LinearLayoutManager(this@HabitsActivity)
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
                val artifact = currentArtifact
                if (artifact == null) {
                    Toast.makeText(this, "La encuesta aún no ha cargado", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
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
                    surveyId       = "680513097959660978ff6e8e",
                    surveyName     = artifact.name,
                    type           = "Behavioral",
                    completionDate = today,
                    responses      = responses
                )
                questionnaireService.addQuestionnaire(request)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                            if (!resp.isSuccessful) {
                                Toast.makeText(this@HabitsActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                                return
                            }

                            // Encuesta enviada OK
                            Toast.makeText(this@HabitsActivity, "Encuesta enviada", Toast.LENGTH_SHORT).show()
                            // Guardas respuestas en prefs
                            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(Date())

                            prefs.edit()
                                .putString("HABITS_SURVEY_DATE", today)
                                .putString("HABITS_ANSWERS", Gson().toJson(request.responses))
                                .apply()

                            // Recompensa por completar encuesta
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
                                                                        Toast.makeText(
                                                                            this@HabitsActivity,
                                                                            "Recompensa de racha aplicada: $streak días",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else {
                                                                        Toast.makeText(
                                                                            this@HabitsActivity,
                                                                            "Error recompensa racha: ${rewardStreakResp.code()}",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                                    goHome()
                                                                }
                                                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                                                    Toast.makeText(
                                                                        this@HabitsActivity,
                                                                        "Fallo recompensa racha: ${t.message}",
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                    goHome()
                                                                }
                                                            })

                                                    } else {
                                                        Toast.makeText(
                                                            this@HabitsActivity,
                                                            "Error al obtener racha: ${streakResp.code()}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        goHome()
                                                    }
                                                }
                                                override fun onFailure(call: Call<Int>, t: Throwable) {
                                                    Toast.makeText(
                                                        this@HabitsActivity,
                                                        "Fallo petición racha: ${t.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
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

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@HabitsActivity, "Fallo envío encuesta: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }
        loadHabitsSurvey(readOnly, idResponse)
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
                                    resp: Response<QuestionnaireResponseDetail>
                                ) {
                                    resp.body()?.responses?.let { surveyAdapter.setResponses(it) }
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
        startActivity(Intent(this,HomeActivity::class.java))
        finish()
    }
}
