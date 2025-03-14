package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    private lateinit var lottieExit: LottieAnimationView
    private lateinit var lottieCard1: LottieAnimationView
    private lateinit var lottieCard2: LottieAnimationView
    private lateinit var lottieCard3: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initLottieAnimations()
        setupCardInteractions()
        setupBottomNavigation()

        listOf(lottieCard1, lottieCard2, lottieCard3).forEach {
            it.repeatCount = 0
            it.playAnimation()
        }
    }

    private fun initLottieAnimations() {
        lottieExit = findViewById(R.id.exit)
        lottieCard1 = findViewById(R.id.ivCard1)
        lottieCard2 = findViewById(R.id.ivCard2)
        lottieCard3 = findViewById(R.id.ivCard3)
    }

    private fun setupCardInteractions() {
        val card1 = findViewById<androidx.cardview.widget.CardView>(R.id.cardContactos)
        val card2 = findViewById<androidx.cardview.widget.CardView>(R.id.cardMetricas)
        val card3 = findViewById<androidx.cardview.widget.CardView>(R.id.cardEliminar)

        card1.setOnClickListener {
            lottieCard1.playAnimation()

        }

        card2.setOnClickListener {
            lottieCard2.playAnimation()

        }

        card3.setOnClickListener {
            lottieCard3.playAnimation()

        }

        lottieExit.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menuPerfil

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuInicio -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.menuJardin -> {
                    startActivity(Intent(this, SpecificContentActivity::class.java))
                    true
                }
                R.id.menuContenido -> {
                    startActivity(Intent(this, ContentActivity::class.java))
                    true
                }
                R.id.menuPerfil -> true
                else -> false
            }
        }
    }
}