package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ContentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content) // Asegúrate de que este XML tenga el mismo nombre que tu layout

        // Configurar BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menuContenido // Resalta la pestaña actual

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuInicio -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                /*R.id.menuSeguimiento -> {
                    startActivity(Intent(this, SeguimientoActivity::class.java))
                    true
                }
                R.id.menuJardin -> {
                    startActivity(Intent(this, JardinActivity::class.java))
                    true
                }*/
                R.id.menuContenido -> true
                /*R.id.menuPerfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }*/
                else -> false
            }
        }

        // Botón de logout
        val exit = findViewById<ImageView>(R.id.exit)
        exit.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
