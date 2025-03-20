package com.example.frontzephiro.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R
import com.google.android.material.card.MaterialCardView

class EmergencyNumbersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_numbers)

        val cardBogota = findViewById<MaterialCardView>(R.id.cardBogota)
        val cardNacional = findViewById<MaterialCardView>(R.id.cardNacional)
        val cardAyudaLinea = findViewById<MaterialCardView>(R.id.cardAyudaLinea)

        cardBogota.setOnClickListener {
            val phoneNumber = "3102996660"
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(dialIntent)
        }

        cardNacional.setOnClickListener {
            val phoneNumber = "3012558747"
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(dialIntent)
        }

        cardAyudaLinea.setOnClickListener {
            val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(webIntent)
        }
    }
}
