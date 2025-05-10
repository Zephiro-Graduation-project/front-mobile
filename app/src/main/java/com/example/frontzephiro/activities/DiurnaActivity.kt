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
import com.example.frontzephiro.databinding.ActivitySurveySmallBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.models.QuestionnaireRequest
import com.example.frontzephiro.models.QuestionnaireResponseDetail
import com.example.frontzephiro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DiurnaActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySurveySmallBinding
    private lateinit var surveyAdapter: SurveyAdapter
    private lateinit var artifactService: ArtifactApiService
    private lateinit var questionnaireService: QuestionnaireApiService
    private lateinit var gamificationService: GamificationApiService

    override fun onCreate(savedInstanceState: Bundle?) {

        gamificationService = RetrofitClient
            .getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)

        super.onCreate(savedInstanceState)
        binding = ActivitySurveySmallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val readOnly   = intent.getBooleanExtra("READ_ONLY", false)
        val idResponse = intent.getStringExtra("ID_RESPONSE")

        surveyAdapter = SurveyAdapter(emptyList(), readOnly)
        binding.rvSurvey.apply {
            layoutManager = LinearLayoutManager(this@DiurnaActivity)
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
                val prefs  = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                val userId = prefs.getString("USER_ID", "") ?: ""
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
                    surveyId       = "DIURNO_2025_04",
                    surveyName     = "Microsurvey Diurno",
                    type           = "Microsurvey",
                    completionDate = today,
                    responses      = responses
                )
                questionnaireService.addQuestionnaire(request)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                            if (!resp.isSuccessful) {
                                Toast.makeText(this@DiurnaActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                                return
                            }

                            // Guarda la fecha y avisa éxito encuesta
                            prefs.edit()
                                .putString("DIURNO_SURVEY_DATE", today)
                                .apply()
                            Toast.makeText(this@DiurnaActivity, "¡Encuesta enviada!", Toast.LENGTH_SHORT).show()

                            // Premia por completar encuesta
                            gamificationService.rewardSurvey(userId)
                                .enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, rewardResp: Response<Void>) {
                                        if (rewardResp.isSuccessful) {
                                            Toast.makeText(
                                                this@DiurnaActivity,
                                                "Recompensa por encuesta aplicada",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@DiurnaActivity,
                                                "Error recompensa encuesta: ${rewardResp.code()}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        // Obtener la racha
                                        questionnaireService.getStreak(userId)
                                            .enqueue(object : Callback<Int> {
                                                override fun onResponse(call: Call<Int>, streakResp: Response<Int>) {
                                                    if (streakResp.isSuccessful) {
                                                        val streak = streakResp.body() ?: 0

                                                        // Premiar la racha
                                                        gamificationService.rewardStreak(userId, streak)
                                                            .enqueue(object : Callback<Void> {
                                                                override fun onResponse(
                                                                    call: Call<Void>,
                                                                    rewardStreakResp: Response<Void>
                                                                ) {
                                                                    if (rewardStreakResp.isSuccessful) {
                                                                        Toast.makeText(
                                                                            this@DiurnaActivity,
                                                                            "Recompensa de racha aplicada: $streak días",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    } else {
                                                                        Toast.makeText(
                                                                            this@DiurnaActivity,
                                                                            "Error recompensa racha: ${rewardStreakResp.code()}",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                                    goHome()
                                                                }
                                                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                                                    Toast.makeText(
                                                                        this@DiurnaActivity,
                                                                        "Fallo recompensa racha: ${t.message}",
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                    goHome()
                                                                }
                                                            })

                                                    } else {
                                                        Toast.makeText(
                                                            this@DiurnaActivity,
                                                            "Error al obtener racha: ${streakResp.code()}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        goHome()
                                                    }
                                                }
                                                override fun onFailure(call: Call<Int>, t: Throwable) {
                                                    Toast.makeText(
                                                        this@DiurnaActivity,
                                                        "Fallo petición racha: ${t.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    goHome()
                                                }
                                            })
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        Toast.makeText(
                                            this@DiurnaActivity,
                                            "Fallo recompensa encuesta: ${t.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        // intentamos racha igual o podrías irte a casa directamente
                                        goHome()
                                    }
                                })
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@DiurnaActivity, "Fallo envío encuesta: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })

            }
        }

        loadDiurnoArtifact(readOnly, idResponse)
    }

    private fun loadDiurnoArtifact(readOnly: Boolean, idResponse: String?) {
        artifactService.getDiurnoArtifact()
            .enqueue(object: Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val art = resp.body() ?: run {
                        Toast.makeText(this@DiurnaActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val toShow = art.questions.filter { q ->
                        q.measures.any { it in listOf("Stress", "Anxiety", "Sleep", "Control") }
                    }
                    surveyAdapter.updateQuestions(toShow)

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
                                    Toast.makeText(this@DiurnaActivity, "Fallo detalle: ${t.message}", Toast.LENGTH_LONG).show()
                                }
                            })
                    }
                }
                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(this@DiurnaActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
