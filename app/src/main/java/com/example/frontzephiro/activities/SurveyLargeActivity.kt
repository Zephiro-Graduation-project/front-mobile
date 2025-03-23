package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R

class SurveyLargeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_large)

        val exit = findViewById<ImageView>(R.id.exit)
        exit.setOnClickListener { logout() }
    }

    private fun logout() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}