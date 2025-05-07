package com.example.frontzephiro.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.adapters.DemographicsAdapter
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.api.GamificationApiService
import com.example.frontzephiro.api.ProfilingApiService
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.databinding.ActivitySurveyLargeDemographicsBinding
import com.example.frontzephiro.models.QuestionnaireRequest
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
    private lateinit var prefs: SharedPreferences
    private lateinit var gamificationService: GamificationApiService

    override fun onCreate(savedInstanceState: Bundle?) {

        gamificationService = RetrofitClient
            .getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)

        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeDemographicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        adapter = DemographicsAdapter(emptyList(), false)
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

        profilingService = RetrofitClient
            .getProfileClient()
            .create(ProfilingApiService::class.java)

        binding.botonEnviar.setOnClickListener { onSendClicked() }
        loadDemographics()
    }

    private fun onSendClicked() {
        val userId = prefs.getString("USER_ID","") ?: ""
        if (userId.isBlank()) {
            Toast.makeText(this,"Usuario no autenticado",Toast.LENGTH_SHORT).show()
            Log.e(TAG,"No hay USER_ID en prefs")
            return
        }
        val age = binding.etEdad.text.toString().trim()
        if (age.isEmpty()) {
            Toast.makeText(this,"Ingresa tu edad",Toast.LENGTH_SHORT).show()
            Log.w(TAG,"Edad vacía")
            return
        }
        val rest = adapter.getResponses()
        if (rest.any{ it.selectedAnswer.isBlank() }) {
            Toast.makeText(this,"Completa todas las preguntas",Toast.LENGTH_SHORT).show()
            Log.w(TAG,"Respuestas incompletas: $rest")
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date())
        val responses = mutableListOf(
            ResponseItem(
                question       = binding.preguntaEdad.text.toString(),
                selectedAnswer = age,
                numericalValue = age.toIntOrNull()?:0,
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

        Log.d(TAG,"Enviando DEMOG: $surveyReq")
        questionnaireService.addQuestionnaire(surveyReq)
            .enqueue(object: Callback<Void>{
                override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                    if (resp.isSuccessful) {
                        Toast.makeText(this@DemographicsActivity,"Encuesta enviada",Toast.LENGTH_SHORT).show()
                        Log.d(TAG,"DEMOG OK (${resp.code()})")
                        createProfile(userId)
                    } else {
                        Toast.makeText(this@DemographicsActivity,"Error envío ${resp.code()}",Toast.LENGTH_SHORT).show()
                        Log.e(TAG,"Error DEMOG: ${resp.code()} – ${resp.errorBody()?.string()}")
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@DemographicsActivity,"Fallo envío: ${t.message}",Toast.LENGTH_LONG).show()
                    Log.e(TAG,"Fallo red DEMOG",t)
                }
            })
    }

    private fun createProfile(userId: String) {
        Log.d(TAG,"Creando perfil userId=$userId")
        profilingService
            .createProfileFromDatabase(userId, "Mongo")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                    if (resp.isSuccessful) {
                        Toast.makeText(
                            this@DemographicsActivity,
                            "Perfil creado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        prefs.edit()
                            .putBoolean("DEMOGRAPHICS_FILLED", true)
                            .apply()
                    } else {
                        Toast.makeText(
                            this@DemographicsActivity,
                            "Error creando perfil: ${resp.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    // → Aquí recompensamos al completar Demographics
                    gamificationService
                        .rewardSurvey(userId)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, rewardResp: Response<Void>) {
                                if (rewardResp.isSuccessful) {
                                    Toast.makeText(
                                        this@DemographicsActivity,
                                        "¡Recompensa aplicada!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@DemographicsActivity,
                                        "Error recompensa: ${rewardResp.code()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                goHome()
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(
                                    this@DemographicsActivity,
                                    "Fallo recompensa: ${t.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                goHome()
                            }
                        })
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@DemographicsActivity,
                        "Fallo al crear perfil: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    // Aunque falle el perfil, aún intentamos dar la recompensa
                    gamificationService
                        .rewardSurvey(userId)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, rewardResp: Response<Void>) {
                                goHome()
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                goHome()
                            }
                        })
                }
            })
    }


    private fun loadDemographics() {
        Log.d(TAG,"Cargando DEMOG preguntas")
        artifactService.getDemographicsArtifact()
            .enqueue(object: Callback<com.example.frontzephiro.models.Artifact>{
                override fun onResponse(call: Call<com.example.frontzephiro.models.Artifact>,
                                        resp: Response<com.example.frontzephiro.models.Artifact>) {
                    resp.body()?.questions?.let {
                        binding.preguntaEdad.text = it.firstOrNull()?.text
                        adapter.updateQuestions(it.drop(1))
                        Log.d(TAG,"Preguntas DEMOG: ${it.size}")
                    } ?: run {
                        Toast.makeText(this@DemographicsActivity,"Error carga DEMOG",Toast.LENGTH_SHORT).show()
                        Log.e(TAG,"DEMOG body nulo o error ${resp.code()}")
                    }
                }
                override fun onFailure(call: Call<com.example.frontzephiro.models.Artifact>, t: Throwable) {
                    Toast.makeText(this@DemographicsActivity,"Fallo red DEMOG",Toast.LENGTH_LONG).show()
                    Log.e(TAG,"Fallo red DEMOG",t)
                }
            })
    }

    private fun goHome() {
        startActivity(Intent(this,HomeActivity::class.java))
        finish()
    }
}

