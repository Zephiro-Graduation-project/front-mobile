package com.example.frontzephiro.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val callAnimation = findViewById<LottieAnimationView>(R.id.call)
        val alertAnimation = findViewById<LottieAnimationView>(R.id.alert)
        callAnimation.repeatCount = 0
        callAnimation.playAnimation()

        alertAnimation.repeatCount = 0
        alertAnimation.playAnimation()

        val userName = sharedPreferences.getString("USER_NAME", "Usuario")
        val namePersona = findViewById<TextView>(R.id.namePersona)
        namePersona.text = "Hola, $userName"

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuInicio -> true

                R.id.menuSeguimiento -> {
                    startActivity(Intent(this, SeguimientoActivity::class.java))
                    true
                }
                R.id.menuJardin -> {
                    startActivity(Intent(this, GardenMain::class.java))
                    true
                }
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
}
