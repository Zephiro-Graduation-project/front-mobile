package com.example.frontzephiro.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.api.GamificationApiService
import com.example.frontzephiro.api.ProfilingApiService
import com.example.frontzephiro.models.ProfileResponse
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        val userName = sharedPreferences.getString("USER_NAME", "Usuario")
        findViewById<TextView>(R.id.namePersona).text = "Hola, $userName"

        findViewById<LottieAnimationView>(R.id.call).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@HomeActivity, EmergencyContactsActivity::class.java))
            }
        }
        findViewById<LottieAnimationView>(R.id.alert).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@HomeActivity, EmergencyNumbersActivity::class.java))
            }
        }

        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menuInicio      -> true
                    R.id.menuSeguimiento -> { startActivity(Intent(this, TrackerMain::class.java)); true }
                    R.id.menuJardin      -> { startActivity(Intent(this, GardenMain::class.java));  true }
                    R.id.menuContenido   -> { startActivity(Intent(this, ContentActivity::class.java)); true }
                    R.id.menuPerfil      -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                    else                 -> false
                }
            }

        loadProfileAndPopulate()
        loadCoinsAndPopulate()
    }

    private fun loadProfileAndPopulate() {
        val userId = sharedPreferences.getString("USER_ID", "") ?: ""
        if (userId.isBlank()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val profilingService = RetrofitClient
            .getProfileClient()
            .create(ProfilingApiService::class.java)

        profilingService.getProfile(userId, "Mongo")
            .enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        val body       = response.body()!!
                        val stressCat  = body.stressIndicator.category
                        val anxietyCat = body.anxietyIndicator.category
                        findViewById<TextView>(R.id.tvDescrpC1).text =
                            "Estrés: $stressCat\nAnsiedad: $anxietyCat"
                    } else {
                        Log.e("HomeActivity", "Error perfil ${response.code()} — ${response.errorBody()?.string()}")
                        Toast.makeText(this@HomeActivity, "No se pudo cargar perfil", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Log.e("HomeActivity", "Fallo red perfil", t)
                    Toast.makeText(this@HomeActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
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

        val gamificationService = RetrofitClient
            .getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)

        gamificationService.getCoins(userId)
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
}
