package com.example.frontzephiro.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.adapters.DemographicsAdapter
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.api.GamificationApiService
import com.example.frontzephiro.api.ProfilingApiService
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
    companion object {
        private const val TAG = "DemographicsAct"
    }

    private lateinit var binding: ActivitySurveyLargeDemographicsBinding
    private lateinit var adapter: DemographicsAdapter
    private lateinit var artifactService: ArtifactApiService
    private lateinit var questionnaireService: QuestionnaireApiService
    private lateinit var profilingService: ProfilingApiService
    private lateinit var gamificationService: GamificationApiService
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeDemographicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Leer flags de Intent
        val readOnly   = intent.getBooleanExtra("READ_ONLY", false)
        val idResponse = intent.getStringExtra("ID_RESPONSE")

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        gamificationService = RetrofitClient
            .getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)
        artifactService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)
        questionnaireService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)
        profilingService = RetrofitClient
            .getProfileClient()
            .create(ProfilingApiService::class.java)

        // 2. Inicializar adapter con flag readOnly
        adapter = DemographicsAdapter(emptyList(), readOnly)
        binding.rvPreguntas.apply {
            layoutManager = LinearLayoutManager(this@DemographicsActivity)
            setHasFixedSize(true)
            adapter = this@DemographicsActivity.adapter
        }

        // 3. Ocultar o mostrar botón y habilitar/deshabilitar campo edad
        if (readOnly) {
            binding.botonEnviar.visibility = View.GONE
            binding.etEdad.isEnabled = false
        } else {
            binding.botonEnviar.setOnClickListener { onSendClicked() }
        }

        // 4. Cargar preguntas y, si toca, respuestas antiguas
        loadDemographics(readOnly, idResponse)
    }

    private fun onSendClicked() {
        val userId = prefs.getString("USER_ID","") ?: ""
        if (userId.isBlank()) {
            Log.e("DemographicsActivity", "No hay usuario autenticado")
            //Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "No hay USER_ID en prefs")
            return
        }
        val age = binding.etEdad.text.toString().trim()
        if (age.isEmpty()) {
            Toast.makeText(this, "Ingresa tu edad", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "Edad vacía")
            return
        }
        val rest = adapter.getResponses()
        if (rest.any { it.selectedAnswer.isBlank() }) {
            Toast.makeText(this, "Completa todas las preguntas", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "Respuestas incompletas: $rest")
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val responses = mutableListOf(
            ResponseItem(
                question       = binding.preguntaEdad.text.toString(),
                selectedAnswer = age,
                numericalValue = age.toIntOrNull() ?: 0,
                measures       = listOf("Demographics")
            )
        ).apply { addAll(rest) }

        val surveyReq = QuestionnaireRequest(
            userId         = userId,
            surveyId       = "680513147959660978ff6e8f",
            surveyName     = "Cuestionario Sociodemográfico",
            type           = "Demographics",
            completionDate = date,
            responses      = responses
        )

        Log.d(TAG, "Enviando DEMOG: $surveyReq")
        questionnaireService.addQuestionnaire(surveyReq)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                    if (resp.isSuccessful) {
                        Toast.makeText(this@DemographicsActivity, "Encuesta enviada", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "DEMOG OK (${resp.code()})")
                        createProfile(userId)
                    } else {
                        Toast.makeText(this@DemographicsActivity, "Error envío ${resp.code()}", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Error DEMOG: ${resp.code()} – ${resp.errorBody()?.string()}")
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@DemographicsActivity, "Fallo envío: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Fallo red DEMOG", t)
                }
            })
    }

    private fun createProfile(userId: String) {
        Log.d(TAG, "Creando perfil userId=$userId")
        profilingService
            .createProfileFromDatabase(userId, "Mongo")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                    if (resp.isSuccessful) {
                        Toast.makeText(this@DemographicsActivity, "Perfil creado correctamente", Toast.LENGTH_SHORT).show()
                        prefs.edit().putBoolean("DEMOGRAPHICS_FILLED", true).apply()
                    } else {
                        Toast.makeText(this@DemographicsActivity, "Error creando perfil: ${resp.code()}", Toast.LENGTH_LONG).show()
                    }

                    // Recompensas
                    gamificationService.rewardSurvey(userId)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, rewardResp: Response<Void>) {
                                if (rewardResp.isSuccessful) {
                                    Toast.makeText(this@DemographicsActivity, "Recompensa por encuesta aplicada", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@DemographicsActivity, "Error recompensa encuesta: ${rewardResp.code()}", Toast.LENGTH_SHORT).show()
                                }

                                questionnaireService.getStreak(userId)
                                    .enqueue(object : Callback<Int> {
                                        override fun onResponse(call: Call<Int>, streakResp: Response<Int>) {
                                            if (streakResp.isSuccessful) {
                                                val streak = streakResp.body() ?: 0
                                                gamificationService.rewardStreak(userId, streak)
                                                    .enqueue(object : Callback<Void> {
                                                        override fun onResponse(call: Call<Void>, rewardStreakResp: Response<Void>) {
                                                            if (rewardStreakResp.isSuccessful) {
                                                                Log.e("DemographicsActivity", "Recompensa de racha aplicada: $streak días")
                                                                //Toast.makeText(this@DemographicsActivity,"Recompensa de racha aplicada: $streak días",Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Log.e("DemographicsActivity", "Error recompensa racha: ${rewardStreakResp.code()}")
                                                                //Toast.makeText(this@DemographicsActivity,"Error recompensa racha: ${rewardStreakResp.code()}",Toast.LENGTH_SHORT).show()
                                                            }
                                                            goHome()
                                                        }
                                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                                            Log.e("DemographicsActivity", "Fallo recompensa racha: ${t.message}")
                                                            //Toast.makeText(this@DemographicsActivity, "Fallo recompensa racha: ${t.message}", Toast.LENGTH_LONG).show()
                                                            goHome()
                                                        }
                                                    })
                                            } else {
                                                Log.e("DemographicsActivity", "Error al obtener racha: ${streakResp.code()}")
                                                //Toast.makeText(this@DemographicsActivity, "Error al obtener racha: ${streakResp.code()}", Toast.LENGTH_SHORT).show()
                                                goHome()
                                            }
                                        }
                                        override fun onFailure(call: Call<Int>, t: Throwable) {
                                            Log.e("DemographicsActivity", "Fallo petición racha: ${t.message}")
                                            //Toast.makeText(this@DemographicsActivity, "Fallo petición racha: ${t.message}", Toast.LENGTH_LONG).show()
                                            goHome()
                                        }
                                    })
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(this@DemographicsActivity, "Fallo recompensa encuesta: ${t.message}", Toast.LENGTH_LONG).show()
                                // Intentar racha igual
                                questionnaireService.getStreak(userId)
                                    .enqueue(object : Callback<Int> {
                                        override fun onResponse(call: Call<Int>, streakResp: Response<Int>) {
                                            goHome()
                                        }
                                        override fun onFailure(call: Call<Int>, t: Throwable) {
                                            goHome()
                                        }
                                    })
                            }
                        })
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@DemographicsActivity, "Fallo al crear perfil: ${t.message}", Toast.LENGTH_LONG).show()
                    gamificationService.rewardSurvey(userId).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, resp: Response<Void>) { goHome() }
                        override fun onFailure(call: Call<Void>, t: Throwable) { goHome() }
                    })
                }
            })
    }

    private fun loadDemographics(readOnly: Boolean, idResponse: String?) {
        Log.d(TAG, "Cargando DEMOG preguntas")
        artifactService.getDemographicsArtifact()
            .enqueue(object : Callback<Artifact> {
                override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                    val questions = resp.body()?.questions
                    if (questions == null) {
                        Toast.makeText(this@DemographicsActivity, "Error carga DEMOG", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "DEMOG body nulo o error ${resp.code()}")
                        return
                    }
                    // Primera pregunta es edad
                    binding.preguntaEdad.text = questions.firstOrNull()?.text
                    adapter.updateQuestions(questions.drop(1))

                    // Si es solo lectura y tengo ID_RESPONSE, cargo respuestas guardadas
                    if (readOnly && idResponse != null) {
                        questionnaireService.getQuestionnaireDetail(idResponse)
                            .enqueue(object : Callback<QuestionnaireResponseDetail> {
                                override fun onResponse(
                                    call: Call<QuestionnaireResponseDetail>,
                                    detailResp: Response<QuestionnaireResponseDetail>
                                ) {
                                    detailResp.body()?.responses?.let { savedResponses ->
                                        // Rellenar edad
                                        val ageResp = savedResponses.firstOrNull()
                                        binding.etEdad.setText(ageResp?.selectedAnswer)
                                        // Rellenar resto en adapter
                                        adapter.setResponses(savedResponses.drop(1))
                                    }
                                }
                                override fun onFailure(call: Call<QuestionnaireResponseDetail>, t: Throwable) {
                                    Toast.makeText(this@DemographicsActivity, "Fallo detalle: ${t.message}", Toast.LENGTH_LONG).show()
                                }
                            })
                    }
                }
                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(this@DemographicsActivity, "Fallo red DEMOG", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Fallo red DEMOG", t)
                }
            })
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
