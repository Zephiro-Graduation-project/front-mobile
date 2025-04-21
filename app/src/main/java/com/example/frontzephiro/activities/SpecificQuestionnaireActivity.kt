package com.example.frontzephiro.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.adapters.DemographicsAdapter
import com.example.frontzephiro.adapters.SurveyAdapter
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.databinding.ActivitySurveyLargeBinding
import com.example.frontzephiro.databinding.ActivitySurveyLargeDemographicsBinding
import com.example.frontzephiro.databinding.ActivitySurveySmallBinding
import com.example.frontzephiro.databinding.ActivitySurveySmallPmBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.models.QuestionnaireResponseDetail
import com.example.frontzephiro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SpecificQuestionnaireActivity : AppCompatActivity() {

    private lateinit var detailService: QuestionnaireApiService
    private var surveyAdapter: SurveyAdapter? = null
    private var demographicsAdapter: DemographicsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Leer extras
        val idResponse = intent.getStringExtra("ID_RESPONSE") ?: run {
            Toast.makeText(this, "❌ Falta ID de respuesta", Toast.LENGTH_SHORT).show()
            finish(); return
        }
        val surveyName = intent.getStringExtra("SURVEY_NAME") ?: run {
            Toast.makeText(this, "❌ Falta nombre de encuesta", Toast.LENGTH_SHORT).show()
            finish(); return
        }
        Toast.makeText(this, "📝 Abriendo “$surveyName” (ID=$idResponse)", Toast.LENGTH_LONG).show()
        Log.d("SPECIFIC", "surveyName=$surveyName, idResponse=$idResponse")

        // 2) Inflar layout y configurar RecyclerView
        var artifactCall: Call<Artifact>? = null
        when {
            surveyName.contains("Diurno") -> {
                val b = ActivitySurveySmallBinding.inflate(layoutInflater)
                setContentView(b.root)
                b.botonEnviar.visibility = View.GONE
                surveyAdapter = SurveyAdapter(emptyList(), readOnly = true)
                b.rvSurvey.layoutManager = LinearLayoutManager(this)
                b.rvSurvey.setHasFixedSize(true)
                b.rvSurvey.adapter = surveyAdapter

                artifactCall = RetrofitClient
                    .getAuthenticatedArtifactClient(this)
                    .create(ArtifactApiService::class.java)
                    .getDiurnoArtifact()
            }
            surveyName.contains("Nocturno") -> {
                val b = ActivitySurveySmallPmBinding.inflate(layoutInflater)
                setContentView(b.root)
                b.botonEnviar.visibility = View.GONE
                surveyAdapter = SurveyAdapter(emptyList(), readOnly = true)
                b.rvSurvey.layoutManager = LinearLayoutManager(this)
                b.rvSurvey.setHasFixedSize(true)
                b.rvSurvey.adapter = surveyAdapter

                artifactCall = RetrofitClient
                    .getAuthenticatedArtifactClient(this)
                    .create(ArtifactApiService::class.java)
                    .getNocturnoArtifact()
            }
            surveyName.contains("PSS") || surveyName.contains("GAD") || surveyName.contains("hábitos") -> {
                val b = ActivitySurveyLargeBinding.inflate(layoutInflater)
                setContentView(b.root)
                b.botonEnviar.visibility = View.GONE
                surveyAdapter = SurveyAdapter(emptyList(), readOnly = true)
                b.rvPreguntas.layoutManager = LinearLayoutManager(this)
                b.rvPreguntas.setHasFixedSize(true)
                b.rvPreguntas.adapter = surveyAdapter

                artifactCall = RetrofitClient
                    .getAuthenticatedArtifactClient(this)
                    .create(ArtifactApiService::class.java)
                    .let {
                        when {
                            surveyName.contains("PSS") -> it.getPssArtifact()
                            surveyName.contains("GAD") -> it.getGadArtifact()
                            else -> it.getHabitsArtifact()
                        }
                    }
            }
            surveyName.contains("Sociodemográfico") -> {
                val b = ActivitySurveyLargeDemographicsBinding.inflate(layoutInflater)
                setContentView(b.root)
                b.botonEnviar.visibility = View.GONE
                demographicsAdapter = DemographicsAdapter(emptyList(), readOnly = true)
                b.rvPreguntas.layoutManager = LinearLayoutManager(this)
                b.rvPreguntas.setHasFixedSize(true)
                b.rvPreguntas.adapter = demographicsAdapter

                artifactCall = RetrofitClient
                    .getAuthenticatedArtifactClient(this)
                    .create(ArtifactApiService::class.java)
                    .getDemographicsArtifact()
            }
            else -> {
                Toast.makeText(this, "❌ Tipo de encuesta no soportado", Toast.LENGTH_SHORT).show()
                finish(); return
            }
        }

        // 3) Inicializar el servicio de detalle
        detailService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)

        // 4) Asegurarnos de que artifactCall no es null
        if (artifactCall == null) {
            Toast.makeText(this, "❌ artifactCall es null", Toast.LENGTH_LONG).show()
            return
        }

        // 5) Cargar la estructura (preguntas)
        artifactCall.enqueue(object : Callback<Artifact> {
            override fun onResponse(call: Call<Artifact>, resp: Response<Artifact>) {
                val art = resp.body()
                if (!resp.isSuccessful || art == null) {
                    Toast.makeText(
                        this@SpecificQuestionnaireActivity,
                        "❌ Error cargando estructura: ${resp.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("SPECIFIC", "Error estructura: ${resp.code()} / ${resp.errorBody()?.string()}")
                    return
                }
                Toast.makeText(
                    this@SpecificQuestionnaireActivity,
                    "✅ Estructura cargada: ${art.questions.size} preguntas",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("SPECIFIC", "Preguntas: ${art.questions.map { it.text }}")

                //  actualizar los adapters
                surveyAdapter?.updateQuestions(art.questions)
                demographicsAdapter?.updateQuestions(art.questions)

                //  luego cargar las respuestas
                loadDetail(idResponse)
            }

            override fun onFailure(call: Call<Artifact>, t: Throwable) {
                Toast.makeText(
                    this@SpecificQuestionnaireActivity,
                    "❌ Fallo estructura: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("SPECIFIC", "onFailure estructura", t)
            }
        })
    }

    private fun loadDetail(idResponse: String) {
        detailService.getQuestionnaireDetail(idResponse)
            .enqueue(object : Callback<QuestionnaireResponseDetail> {
                override fun onResponse(
                    call: Call<QuestionnaireResponseDetail>,
                    resp: Response<QuestionnaireResponseDetail>
                ) {
                    val detail = resp.body()
                    if (!resp.isSuccessful || detail == null) {
                        Toast.makeText(
                            this@SpecificQuestionnaireActivity,
                            "❌ Error cargando respuestas: ${resp.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("SPECIFIC", "Error detalle: ${resp.code()} / ${resp.errorBody()?.string()}")
                        return
                    }
                    Toast.makeText(
                        this@SpecificQuestionnaireActivity,
                        "✅ Respuestas cargadas: ${detail.responses.size}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("SPECIFIC", "Respuestas: ${detail.responses}")

                    // inyectar en los adapters
                    surveyAdapter?.setResponses(detail.responses)
                    demographicsAdapter?.setResponses(detail.responses)
                }

                override fun onFailure(call: Call<QuestionnaireResponseDetail>, t: Throwable) {
                    Toast.makeText(
                        this@SpecificQuestionnaireActivity,
                        "❌ Fallo cargando respuestas: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("SPECIFIC", "onFailure detalle", t)
                }
            })
    }
}
