package com.example.frontzephiro.activities

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.api.GamificationApiService
import com.example.frontzephiro.api.ProfilingApiService
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.models.ProfileResponse
import com.example.frontzephiro.models.QuestionnaireSummary
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    // 1) Lista “quemada” de títulos y URLs de YouTube
    private val recommendedVideos = listOf(
        "RELAJACIÓN GUIADA para calmar la ansiedad y el estrés"            to "https://www.youtube.com/watch?v=_OeDhehaFOE",
        "Relajación guiada para dormir y reducir la ansiedad o el estrés"   to "https://www.youtube.com/watch?v=VIDEO_ID_2",
        "Meditación para el estrés y la ansiedad"                         to "https://www.youtube.com/watch?v=dnid54rqQVc",
        "MEDITACIÓN para DEJAR DE PENSAR, aliviar estrés y ansiedad"       to "https://www.youtube.com/watch?v=hTKDQ5FRB18",
        "Meditación Guiada para la ANSIEDAD"                              to "https://www.youtube.com/watch?v=AsKqvEn0CXc",
        "Ejercicios para controlar la ansiedad"                           to "https://www.youtube.com/watch?v=ifKLyrl2mTk",
        "Tips de relajación para reducir el estrés y la ansiedad"         to "https://www.youtube.com/watch?v=tUXzvOwDkXQ"
    )

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var questionnaireService: QuestionnaireApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        val userName = sharedPreferences.getString("USER_NAME", "Usuario")
        findViewById<TextView>(R.id.namePersona).text = "Hola, $userName"

        findViewById<LottieAnimationView>(R.id.call).apply {
            repeatCount = 0
            playAnimation()
            setOnClickListener {
                startActivity(Intent(this@HomeActivity, EmergencyContactsActivity::class.java))
            }
        }
        findViewById<LottieAnimationView>(R.id.alert).apply {
            repeatCount = 0
            playAnimation()
            setOnClickListener {
                startActivity(Intent(this@HomeActivity, EmergencyNumbersActivity::class.java))
            }
        }

        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menuInicio      -> true
                    R.id.menuSeguimiento -> {
                        startActivity(Intent(this, TrackerMain::class.java))
                        true
                    }
                    R.id.menuJardin      -> {
                        startActivity(Intent(this, GardenMain::class.java))
                        true
                    }
                    R.id.menuContenido   -> {
                        startActivity(Intent(this, ContentActivity::class.java))
                        true
                    }
                    R.id.menuPerfil      -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
    }

    override fun onResume() {
        super.onResume()
        loadProfileAndPopulate()
        loadCoinsAndPopulate()
        loadSurveyCheckAndPopulate()
        loadStreakAndPopulate()
    }

    private fun loadProfileAndPopulate() {
        val userId = sharedPreferences.getString("USER_ID", "") ?: ""
        if (userId.isBlank()) {
            Log.e("HomeActivity", "Usuario no autenticado")
            // Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient
            .getProfileClient()
            .create(ProfilingApiService::class.java)
            .getProfile(userId, "Mongo")
            .enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { body ->
                            val stressCat  = body.stressIndicator.category
                            val anxietyCat = body.anxietyIndicator.category
                            findViewById<TextView>(R.id.tvDescrpC1).text =
                                "Estrés: $stressCat\nAnsiedad: $anxietyCat"
                        }
                    } else {
                        Log.e("HomeActivity", "Error perfil ${response.code()} — ${response.errorBody()?.string()}")
                        //Toast.makeText(this@HomeActivity, "No se pudo cargar perfil", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Log.e("HomeActivity", "Fallo red perfil", t)
                    //Toast.makeText(this@HomeActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadCoinsAndPopulate() {
        val userId = sharedPreferences.getString("USER_ID", "") ?: ""
        val tvCoins = findViewById<TextView>(R.id.tvCoins)

        if (userId.isBlank()) {
            tvCoins.text = "Usuario no autenticado"
            return
        }

        RetrofitClient
            .getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)
            .getCoins(userId)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        val coins = response.body() ?: 0
                        tvCoins.text = "¡Tienes $coins monedas!"
                    } else {
                        tvCoins.text = "Error cargando monedas"
                        Log.e("HomeActivity", "Error monedas ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<Int>, t: Throwable) {
                    tvCoins.text = "No se pudo cargar monedas"
                    Log.e("HomeActivity", "Fallo red monedas", t)
                }
            })
    }

    private fun loadSurveyCheckAndPopulate() {
        val userId = sharedPreferences.getString("USER_ID", "").orEmpty()
        if (userId.isBlank()) {
            Log.e("HomeActivity", "Usuario no autenticado")
            //Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val dateExtra = intent.getStringExtra("SELECTED_DATE")
        val date = dateExtra.takeIf { !it.isNullOrBlank() } ?: run {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            sdf.format(java.util.Date())
        }

        RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)
            .getQuestionnairesOnDate(userId, date)
            .enqueue(object : Callback<List<QuestionnaireSummary>> {
                override fun onResponse(call: Call<List<QuestionnaireSummary>>, response: Response<List<QuestionnaireSummary>>) {
                    if (!response.isSuccessful) {
                        Log.e("HomeActivity", "Error check cuestionarios ${response.code()} — ${response.errorBody()?.string()}")
                        //Toast.makeText(this@HomeActivity, "No se pudo verificar registros diarios", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val list = response.body().orEmpty()
                    val respondedDiurna   = list.any { it.name.contains("Diurno", ignoreCase = true) }
                    val respondedNocturna = list.any { it.name.contains("Nocturno", ignoreCase = true) }

                    val textDiurna   = if (respondedDiurna)   "Respondido" else "No lo has respondido"
                    val textNocturna = if (respondedNocturna) "Respondido" else "No lo has respondido"

                    findViewById<TextView>(R.id.tvDescrpC4).text =
                        "Diurno: $textDiurna\nNocturno: $textNocturna"
                }
                override fun onFailure(call: Call<List<QuestionnaireSummary>>, t: Throwable) {
                    Log.e("HomeActivity", "Fallo de red check cuestionarios", t)
                    //Toast.makeText(this@HomeActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadStreakAndPopulate() {
        questionnaireService = RetrofitClient
            .getAuthenticatedArtifactClient(this)
            .create(QuestionnaireApiService::class.java)

        val prefs  = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "") ?: ""
        val tvStreak = findViewById<TextView>(R.id.tvDescrpC5)

        if (userId.isBlank()) {
            tvStreak.text = "Usuario no autenticado"
            return
        }

        questionnaireService.getStreak(userId)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, streakResp: Response<Int>) {
                    if (streakResp.isSuccessful) {
                        val streak = streakResp.body() ?: 0
                        tvStreak.text = "$streak Dias en racha!"
                    } else {
                        Log.e("HomeActivity", "Error al obtener racha: ${streakResp.code()}")
                        //Toast.makeText(this@HomeActivity,"Error al obtener racha: ${streakResp.code()}",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.e("HomeActivity", "Fallo petición racha: ${t.message}")
                    //Toast.makeText(this@HomeActivity,"Fallo petición racha: ${t.message}",Toast.LENGTH_LONG).show()
                }
            })
    }
}
