package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.adapters.QuestionariesAdapter
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.databinding.ActivityShowQuestionnariesBinding
import com.example.frontzephiro.models.QuestionnaireSummary
import com.example.frontzephiro.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowQuestionnariesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowQuestionnariesBinding
    private lateinit var service: QuestionnaireApiService
    private lateinit var adapter: QuestionariesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowQuestionnariesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón atrás
        findViewById<LottieAnimationView>(R.id.back).apply {
            repeatCount = 0; playAnimation()
        }

        binding.back.setOnClickListener { finish() }

        // Adapter con callback
        adapter = QuestionariesAdapter(emptyList()) { summary ->
            openSurveyInReadOnlyMode(summary)
        }
        binding.rvQuestionnaires.apply {
            layoutManager = LinearLayoutManager(this@ShowQuestionnariesActivity)
            setHasFixedSize(true)
            adapter = this@ShowQuestionnariesActivity.adapter
        }

        // Retrofit
        service = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)

        // Leer extras
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val date   = intent.getStringExtra("SELECTED_DATE") ?: ""
        if (userId.isBlank() || date.isBlank()) {
            Toast.makeText(this, "Faltan datos para cargar cuestionarios", Toast.LENGTH_SHORT).show()
            return
        }

        // Cargar la lista
        service.getQuestionnairesOnDate(userId, date)
            .enqueue(object : Callback<List<QuestionnaireSummary>> {
                override fun onResponse(
                    call: Call<List<QuestionnaireSummary>>,
                    resp: Response<List<QuestionnaireSummary>>
                ) {
                    val list = resp.body().orEmpty()
                    if (list.isEmpty()) {
                        Toast.makeText(
                            this@ShowQuestionnariesActivity,
                            "No hay cuestionarios para $date",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        adapter.updateItems(list)
                    }
                }
                override fun onFailure(call: Call<List<QuestionnaireSummary>>, t: Throwable) {
                    Toast.makeText(
                        this@ShowQuestionnariesActivity,
                        "Fallo de red: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun openSurveyInReadOnlyMode(summary: QuestionnaireSummary) {
        val intent = when {
            summary.name.contains("Diurno") ->
                Intent(this, DiurnaActivity::class.java)
            summary.name.contains("Nocturno") ->
                Intent(this, NocturnaActivity::class.java)
            summary.name.contains("PSS") ->
                Intent(this, PssActivity::class.java)
            summary.name.contains("GAD") ->
                Intent(this, GadActivity::class.java)
            summary.name.contains("hábitos") ->
                Intent(this, HabitsActivity::class.java)
            summary.name.contains("Sociodemográfico") ->
                Intent(this, DemographicsActivity::class.java)
            else -> {
                Toast.makeText(this, "Encuesta no soportada", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // flag, id y nombre para que la pantalla sepa qué inflar y qué detalle pedir
        intent.putExtra("READ_ONLY", true)
        intent.putExtra("ID_RESPONSE", summary.id)
        intent.putExtra("SURVEY_NAME", summary.name)
        startActivity(intent)
    }
}
