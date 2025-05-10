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
import com.example.frontzephiro.api.ProfilingApiService
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

class GadActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "GadActivity"
    }

    private lateinit var binding: ActivitySurveyLargeBinding
    private lateinit var surveyAdapter: SurveyAdapter
    private lateinit var artifactService: ArtifactApiService
    private lateinit var questionnaireService: QuestionnaireApiService
    private lateinit var profilingService: ProfilingApiService
    private lateinit var gamificationService: GamificationApiService

    override fun onCreate(savedInstanceState: Bundle?) {

        gamificationService = RetrofitClient
            .getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)

        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        artifactService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)
        questionnaireService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)
        profilingService = RetrofitClient
            .getProfileClient()
            .create(ProfilingApiService::class.java)

        val readOnly   = intent.getBooleanExtra("READ_ONLY", false)
        val idResponse = intent.getStringExtra("ID_RESPONSE")

        surveyAdapter = SurveyAdapter(emptyList(), readOnly)
        binding.rvPreguntas.apply {
            layoutManager = LinearLayoutManager(this@GadActivity)
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
            adapter = surveyAdapter
        }

        if (!readOnly) {
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
                    surveyId       = "680118d17b11356ba78f4ec9",
                    surveyName     = "Cuestionario de Ansiedad Generalizada (GAD-7)",
                    type           = "Psychological",
                    completionDate = today,
                    responses      = responses
                )

                questionnaireService.addQuestionnaire(request)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                            if (!resp.isSuccessful) {
                                Toast.makeText(this@GadActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "Error en POST encuesta: ${resp.code()} – ${resp.errorBody()?.string()}")
                                return
                            }

                            // 1) Éxito al enviar encuesta
                            Toast.makeText(this@GadActivity, "Encuesta enviada", Toast.LENGTH_SHORT).show()
                            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(Date())

                            prefs.edit()
                                .putString("GAD_SURVEY_DATE", today)
                                .putString("GAD_ANSWERS", Gson().toJson(surveyAdapter.getResponses()))
                                .apply()

                            // 2) Actualizar perfil
                            profilingService.createProfileFromDatabase(userId)
                                .enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, r: Response<Void>) {
                                        if (r.isSuccessful) {
                                            Log.d(TAG, "PUT profile exitoso (code ${r.code()})")
                                        } else {
                                            Log.e(TAG, "Error en PUT profile: ${r.code()} – ${r.errorBody()?.string()}")
                                        }

                                        // 1) Recompensa por completar GAD-7
                                        gamificationService.rewardSurvey(userId)
                                            .enqueue(object : Callback<Void> {
                                                override fun onResponse(call: Call<Void>, rewardResp: Response<Void>) {
                                                    if (rewardResp.isSuccessful) {
                                                        Log.d(TAG, "Recompensa GAD exitosa")
                                                    } else {
                                                        Log.e(TAG, "Error recompensa GAD: ${rewardResp.code()}")
                                                    }

                                                    // 2) Obtener la racha actual
                                                    questionnaireService.getStreak(userId)
                                                        .enqueue(object : Callback<Int> {
                                                            override fun onResponse(call: Call<Int>, streakResp: Response<Int>) {
                                                                if (streakResp.isSuccessful) {
                                                                    val streak = streakResp.body() ?: 0

                                                                    // 3) Recompensa de racha
                                                                    gamificationService.rewardStreak(userId, streak)
                                                                        .enqueue(object : Callback<Void> {
                                                                            override fun onResponse(
                                                                                call: Call<Void>,
                                                                                rewardStreakResp: Response<Void>
                                                                            ) {
                                                                                if (rewardStreakResp.isSuccessful) {
                                                                                    Log.d(
                                                                                        TAG,
                                                                                        "Recompensa racha GAD aplicada: $streak días"
                                                                                    )
                                                                                } else {
                                                                                    Log.e(
                                                                                        TAG,
                                                                                        "Error recompensa racha GAD: ${rewardStreakResp.code()}"
                                                                                    )
                                                                                }
                                                                                goDemographics()
                                                                            }

                                                                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                                                                Log.e(
                                                                                    TAG,
                                                                                    "Fallo recompensa racha GAD: ${t.message}",
                                                                                    t
                                                                                )
                                                                                goDemographics()
                                                                            }
                                                                        })

                                                                } else {
                                                                    Log.e(
                                                                        TAG,
                                                                        "Error al obtener racha GAD: ${streakResp.code()}"
                                                                    )
                                                                    goDemographics()
                                                                }
                                                            }

                                                            override fun onFailure(call: Call<Int>, t: Throwable) {
                                                                Log.e(
                                                                    TAG,
                                                                    "Fallo petición racha GAD: ${t.message}",
                                                                    t
                                                                )
                                                                goDemographics()
                                                            }
                                                        })
                                                }

                                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                                    Log.e(TAG, "Fallo recompensa GAD: ${t.message}", t)
                                                    // Si falla la recompensa por encuesta, saltamos directamente a demographics
                                                    goDemographics()
                                                }
                                            })
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        Log.e(TAG, "Fallo red PUT profile", t)

                                        // Aunque falle el perfil, seguimos con recompensa por encuesta y luego racha
                                        gamificationService.rewardSurvey(userId)
                                            .enqueue(object : Callback<Void> {
                                                override fun onResponse(call: Call<Void>, rewardResp: Response<Void>) {
                                                    // (idéntico al bloque anterior)
                                                    if (rewardResp.isSuccessful) {
                                                        Log.d(TAG, "Recompensa GAD exitosa")
                                                    } else {
                                                        Log.e(TAG, "Error recompensa GAD: ${rewardResp.code()}")
                                                    }
                                                    // Cadena racha...
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
                                                                                    Log.d(
                                                                                        TAG,
                                                                                        "Recompensa racha GAD aplicada: $streak días"
                                                                                    )
                                                                                } else {
                                                                                    Log.e(
                                                                                        TAG,
                                                                                        "Error recompensa racha GAD: ${rewardStreakResp.code()}"
                                                                                    )
                                                                                }
                                                                                goDemographics()
                                                                            }

                                                                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                                                                Log.e(
                                                                                    TAG,
                                                                                    "Fallo recompensa racha GAD: ${t.message}",
                                                                                    t
                                                                                )
                                                                                goDemographics()
                                                                            }
                                                                        })
                                                                } else {
                                                                    Log.e(
                                                                        TAG,
                                                                        "Error al obtener racha GAD: ${streakResp.code()}"
                                                                    )
                                                                    goDemographics()
                                                                }
                                                            }

                                                            override fun onFailure(call: Call<Int>, t: Throwable) {
                                                                Log.e(TAG, "Fallo petición racha GAD: ${t.message}", t)
                                                                goDemographics()
                                                            }
                                                        })
                                                }

                                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                                    goDemographics()
                                                }
                                            })
                                    }
                                })

                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@GadActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                            Log.e(TAG, "Fallo red POST encuesta", t)
                        }
                    })
            }
        } else {
            binding.botonEnviar.visibility = View.GONE
        }

        loadGadSurvey(readOnly, idResponse)
    }

    private fun loadGadSurvey(readOnly: Boolean, idResponse: String?) {
        artifactService.getGadArtifact()
            .enqueue(object : Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val art = resp.body()
                    if (!resp.isSuccessful || art == null) {
                        Toast.makeText(this@GadActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Error cargando GAD artifact: ${resp.code()} – ${resp.errorBody()?.string()}")
                        return
                    }
                    surveyAdapter.updateQuestions(art.questions)
                    if (readOnly && idResponse != null) {
                        questionnaireService.getQuestionnaireDetail(idResponse)
                            .enqueue(object : Callback<QuestionnaireResponseDetail> {
                                override fun onResponse(
                                    call: Call<QuestionnaireResponseDetail>,
                                    r: Response<QuestionnaireResponseDetail>
                                ) {
                                    r.body()?.responses?.let { surveyAdapter.setResponses(it) }
                                }
                                override fun onFailure(call: Call<QuestionnaireResponseDetail>, t: Throwable) {
                                    Toast.makeText(this@GadActivity, "Fallo detalle: ${t.message}", Toast.LENGTH_LONG).show()
                                    Log.e(TAG, "Fallo red detalle GAD", t)
                                }
                            })
                    }
                }
                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(this@GadActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Fallo red GAD artifact", t)
                }
            })
    }

    private fun goDemographics() {
        val prefs    = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val demoDone = prefs.getBoolean("DEMOGRAPHICS_FILLED", false)
        val target   = if (demoDone) HomeActivity::class.java else DemographicsActivity::class.java
        startActivity(Intent(this, target))
        finish()
    }
}
