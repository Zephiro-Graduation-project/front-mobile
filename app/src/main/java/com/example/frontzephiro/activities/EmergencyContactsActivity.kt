package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EmergencyContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contacts)

        val btnCreateEmergencyContact = findViewById<FloatingActionButton>(R.id.floatingAddContact)

        btnCreateEmergencyContact.setOnClickListener {
            val intent = Intent(this, CreateEmergencyContactActivity::class.java)
            startActivity(intent)
        }

    }
}