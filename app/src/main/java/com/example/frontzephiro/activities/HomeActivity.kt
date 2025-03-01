package com.example.frontzephiro.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", "")

        //val textViewToken = findViewById<TextView>(R.id.textViewToken)
        //textViewToken.text = "Token: $token"

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuInicio -> true
                /*
                R.id.menuSeguimiento -> {
                    startActivity(Intent(this, SeguimientoActivity::class.java))
                    true
                }*/
                R.id.menuJardin -> {
                    startActivity(Intent(this, SpecificContentActivity::class.java))
                    true
                }
                R.id.menuContenido -> {
                    startActivity(Intent(this, ContentActivity::class.java))
                    true
                }/*
                R.id.menuPerfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }*/
                else -> false
            }
        }

        val exit = findViewById<ImageView>(R.id.exit)
        exit.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.remove("TOKEN")
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
