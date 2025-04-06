package com.example.frontzephiro.activities

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R

class SeguimientoActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_small_pm)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

    }
}