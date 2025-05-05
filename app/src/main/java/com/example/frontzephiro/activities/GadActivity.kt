// GadActivity.kt
package com.example.frontzephiro.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontzephiro.adapters.SurveyAdapter
import com.example.frontzephiro.api.ArtifactApiService
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.databinding.ActivitySurveyLargeBinding
import com.example.frontzephiro.models.Artifact
import com.example.frontzephiro.models.QuestionnaireRequest
import com.example.frontzephiro.models.QuestionnaireResponseDetail
import com.example.frontzephiro.network.RetrofitClient
import com.example.frontzephiro.receivers.HabitAlarmReceiver
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

class GadActivity : AppCompatActivity() {

    //val context = LocalContext.current
    private lateinit var binding: ActivitySurveyLargeBinding
    private lateinit var surveyAdapter: SurveyAdapter
    private lateinit var artifactService: ArtifactApiService
    private lateinit var questionnaireService: QuestionnaireApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyLargeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val readOnly   = intent.getBooleanExtra("READ_ONLY", false)
        val idResponse = intent.getStringExtra("ID_RESPONSE")

        surveyAdapter = SurveyAdapter(emptyList(), readOnly)
        binding.rvPreguntas.apply {
            layoutManager = LinearLayoutManager(this@GadActivity)
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
            adapter = surveyAdapter
        }

        artifactService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(ArtifactApiService::class.java)
        questionnaireService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)

        //val prefs = getSharedPreferences("zephiro_prefs", Context.MODE_PRIVATE)
        //val yaSeHizo = prefs.getInt("respuesta_unica", 0) == 1

//        if (!yaSeHizo) {
//            envioUnico(this, 1)
//        }

        // Botón de envío si no es solo lectura
        if (readOnly) {
            binding.botonEnviar.visibility = View.GONE
        } else {
            binding.botonEnviar.setOnClickListener {
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
                    surveyId       = "680118d17b11356ba78f4ec9",
                    surveyName     = "Cuestionario de Ansiedad Generalizada (GAD‑7)",
                    type           = "Psychological",
                    completionDate = today,
                    responses      = responses
                )

                questionnaireService.addQuestionnaire(request)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, resp: Response<Void>) {
                            if (resp.isSuccessful) {
                                Toast.makeText(this@GadActivity, "Encuesta enviada", Toast.LENGTH_SHORT).show()

                                // ← Aquí guardas el JSON de las respuestas GAD
                                val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                                prefs.edit()
                                    .putString("GAD_ANSWERS", Gson().toJson(responses))
                                    .apply()

                                goDemographics()
                            } else {
                                Toast.makeText(this@GadActivity, "Error ${resp.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@GadActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            }
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
                                    Toast.makeText(this@GadActivity, "Fallo detalle: ${t.message}", Toast.LENGTH_LONG).show()
                                }
                            })
                    }
                }
                override fun onFailure(call: Call<Artifact>, t: Throwable) {
                    Toast.makeText(this@GadActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun goDemographics() {

        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val demoDone = prefs.getBoolean("DEMOGRAPHICS_FILLED", false)

        val target = if (demoDone) {
            HomeActivity::class.java
        } else {
            DemographicsActivity::class.java
        }

        startActivity(Intent(this, target))
        scheduleReLaunchHabits()
        finish()
    }

    private fun scheduleReLaunchHabits() {
        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // En Android 12+ comprueba permiso antes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmMgr.canScheduleExactAlarms()) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            )
            return
        }

        // Intent para el BroadcastReceiver
        val intent = Intent(this, HabitAlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Ahora + 15 minutos (o 1 min para pruebas)
        val triggerAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)

        alarmMgr.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pending
        )
    }

}
