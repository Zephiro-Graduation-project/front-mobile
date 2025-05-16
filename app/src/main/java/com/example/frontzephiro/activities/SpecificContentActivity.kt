package com.example.frontzephiro.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.api.ContentApiService
import com.example.frontzephiro.models.Content
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SpecificContentActivity : AppCompatActivity() {

    private lateinit var callAnimation: LottieAnimationView
    private lateinit var alertAnimation: LottieAnimationView
    private lateinit var backContainer: LinearLayout
    private lateinit var tvTitle: TextView
    private lateinit var tvAuthorValue: TextView
    private lateinit var resumeArticle: TextView
    private lateinit var chipGroupDetail: ChipGroup
    private lateinit var botonEnlace: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific_content)

        // Vistas
        callAnimation        = findViewById(R.id.call)
        alertAnimation       = findViewById(R.id.alert)
        backContainer        = findViewById(R.id.backContainer)
        tvTitle              = findViewById(R.id.nameArticulo)
        tvAuthorValue        = findViewById(R.id.tvAuthorValue)
        resumeArticle        = findViewById(R.id.ResumeArticle)
        chipGroupDetail      = findViewById(R.id.chipGroupDetail)
        botonEnlace          = findViewById(R.id.botondos)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Animaciones y navegación de cabecera
        callAnimation.repeatCount  = 0
        callAnimation.playAnimation()
        alertAnimation.repeatCount = 0
        alertAnimation.playAnimation()
        backContainer.setOnClickListener { onBackPressed() }
        callAnimation.setOnClickListener {
            startActivity(Intent(this, EmergencyContactsActivity::class.java))
        }
        alertAnimation.setOnClickListener {
            startActivity(Intent(this, EmergencyNumbersActivity::class.java))
        }

        // Bottom nav
        bottomNavigationView.selectedItemId = R.id.menuContenido
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuInicio     -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.menuSeguimiento-> startActivity(Intent(this, TrackerMain::class.java))
                R.id.menuJardin     -> startActivity(Intent(this, GardenMain::class.java))
                R.id.menuPerfil     -> startActivity(Intent(this, ProfileActivity::class.java))
                else                -> return@setOnItemSelectedListener false
            }
            true
        }

        val contentId = intent.getStringExtra("CONTENT_ID")
        if (contentId.isNullOrBlank()) {
            Toast.makeText(this, "Falta ID de contenido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        RetrofitClient.getContentClient()
            .create(ContentApiService::class.java)
            .getContentById(contentId)
            .enqueue(object : Callback<Content> {
                override fun onResponse(call: Call<Content>, resp: Response<Content>) {
                    if (!resp.isSuccessful || resp.body() == null) {
                        Toast.makeText(
                            this@SpecificContentActivity,
                            "Error ${resp.code()} al cargar artículo",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    populateContent(resp.body()!!)
                }
                override fun onFailure(call: Call<Content>, t: Throwable) {
                    Toast.makeText(
                        this@SpecificContentActivity,
                        "Fallo de red: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun populateContent(content: Content) {
        tvTitle.text       = content.name
        tvAuthorValue.text = content.author
        resumeArticle.text = content.description

        chipGroupDetail.removeAllViews()
        content.tags.forEach { tag ->
            Chip(this).apply {
                text = tag.name
                isClickable = false
                isCheckable = false
                chipGroupDetail.addView(this)
            }
        }

        botonEnlace.setOnClickListener {
            var url = content.url.trim()
            if (url.isBlank()) {
                Toast.makeText(this, "Enlace inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!url.startsWith("http://", ignoreCase = true) &&
                !url.startsWith("https://", ignoreCase = true)) {
                url = "https://$url"
            }

            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(this, Uri.parse(url))
        }
    }
}
