package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.adapters.AchievementAdapter
import com.example.frontzephiro.api.GamificationApiService
import com.example.frontzephiro.models.Achievement
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView

class GardenAchievements : AppCompatActivity() {

    private lateinit var recyclerViewLogros: RecyclerView
    private lateinit var achievementAdapter: AchievementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garden_achievements)

        val callAnimation = findViewById<LottieAnimationView>(R.id.call)
        val alertAnimation = findViewById<LottieAnimationView>(R.id.alert)
        val backContainer = findViewById<LinearLayout>(R.id.backContainer)
        callAnimation.repeatCount = 0
        callAnimation.playAnimation()

        alertAnimation.repeatCount = 0
        alertAnimation.playAnimation()

        backContainer.setOnClickListener {
            onBackPressed()
        }

        callAnimation.setOnClickListener {
            val intent = Intent(this, EmergencyContactsActivity::class.java)
            startActivity(intent)
        }

        alertAnimation.setOnClickListener {
            val intent = Intent(this, EmergencyNumbersActivity::class.java)
            startActivity(intent)
        }

        recyclerViewLogros = findViewById(R.id.recycler_logros)

        loadAchievements { achievements ->
            recyclerViewLogros.layoutManager = LinearLayoutManager(this)

            achievementAdapter = AchievementAdapter(achievements)

            recyclerViewLogros.adapter = achievementAdapter
        }



        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menuJardin
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuInicio -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.menuSeguimiento -> {
                    startActivity(Intent(this, TrackerMain::class.java))
                    true
                }
                /*
                R.id.menuJardin -> {
                    startActivity(Intent(this, GardenMain::class.java))
                    true
                } */
                R.id.menuContenido -> {
                    startActivity(Intent(this, ContentActivity::class.java))
                    true
                }
                R.id.menuPerfil -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadAchievements(onAchievementsLoaded: (List<Achievement>) -> Unit) {
        val achievementsApi = RetrofitClient.getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)
        val call = achievementsApi.getAchievements() // Asegúrate de tener este método en tu API

        call.enqueue(object : retrofit2.Callback<List<Achievement>> {
            override fun onResponse(
                call: retrofit2.Call<List<Achievement>>,
                response: retrofit2.Response<List<Achievement>>
            ) {
                if (response.isSuccessful) {
                    val achievements = response.body() ?: emptyList()
                    Log.d("AchievementsActivity", "Se recibieron ${achievements.size} logros")
                    onAchievementsLoaded(achievements)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AchievementsActivity", "Error al cargar logros: $errorBody")
                    Log.e("Retrofit", "Error en la respuesta: ${response.code()}")
                    Log.e("Retrofit", "Error body: $errorBody")
                    Toast.makeText(this@GardenAchievements, "Error al cargar logros", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Achievement>>, t: Throwable) {
                Log.e("AchievementsActivity", "Fallo de conexión", t)
                Toast.makeText(this@GardenAchievements, "Error de conexión al cargar logros", Toast.LENGTH_SHORT).show()
            }
        })
    }


}