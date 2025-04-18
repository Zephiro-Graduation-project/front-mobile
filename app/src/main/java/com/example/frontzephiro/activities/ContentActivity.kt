package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.adapters.ContentAdapter
import com.example.frontzephiro.api.ContentApiService
import com.example.frontzephiro.api.TagsApiService
import com.example.frontzephiro.models.Content
import com.example.frontzephiro.models.Tag
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ContentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        val callAnimation = findViewById<LottieAnimationView>(R.id.call)
        val alertAnimation = findViewById<LottieAnimationView>(R.id.alert)
        callAnimation.repeatCount = 0
        callAnimation.playAnimation()

        alertAnimation.repeatCount = 0
        alertAnimation.playAnimation()

        callAnimation.setOnClickListener {
            val intent = Intent(this, EmergencyContactsActivity::class.java)
            startActivity(intent)
        }

        alertAnimation.setOnClickListener {
            val intent = Intent(this, EmergencyNumbersActivity::class.java)
            startActivity(intent)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menuContenido
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
                R.id.menuJardin -> {
                    startActivity(Intent(this, GardenMain::class.java))
                    true
                }
                /*
                R.id.menuContenido -> {
                    true
                } */
                R.id.menuPerfil -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        loadContent()
        loadTags()
    }

    private fun logout() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun loadContent() {
        val contentApi = RetrofitClient.getAuthenticatedContentClient(this)
            .create(ContentApiService::class.java)
        val call = contentApi.getAllContent()
        call.enqueue(object : retrofit2.Callback<List<Content>> {
            override fun onResponse(
                call: retrofit2.Call<List<Content>>,
                response: retrofit2.Response<List<Content>>
            ) {
                if (response.isSuccessful) {
                    val contentList = response.body() ?: emptyList()
                    Log.d("ContentActivity", "Se recibió contenido: ${contentList.size} items")
                    val recyclerView = findViewById<RecyclerView>(R.id.rvContent)
                    recyclerView.layoutManager = LinearLayoutManager(this@ContentActivity)
                    recyclerView.adapter = ContentAdapter(contentList)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ContentActivity", "Error al cargar contenido: $errorBody")
                    Toast.makeText(this@ContentActivity, "Error al cargar contenido", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<List<Content>>, t: Throwable) {
                Toast.makeText(this@ContentActivity, "Error de conexión al cargar contenido", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadTags() {
        val tagsApi = RetrofitClient.getContentClient().create(TagsApiService::class.java)
        val call = tagsApi.getAllTags()
        call.enqueue(object : retrofit2.Callback<List<Tag>> {
            override fun onResponse(
                call: retrofit2.Call<List<Tag>>,
                response: retrofit2.Response<List<Tag>>
            ) {
                if (response.isSuccessful) {
                    val tags = response.body()
                    Log.d("ContentActivity", "Se recibieron ${tags?.size ?: 0} tags")
                    val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
                    chipGroup.removeAllViews()
                    tags?.forEach { tag ->
                        val chip = Chip(this@ContentActivity)
                        chip.text = tag.name
                        chip.isClickable = true
                        chip.isCheckable = false
                        chipGroup.addView(chip)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ContentActivity", "Error al cargar tags: $errorBody")
                    Toast.makeText(this@ContentActivity, "Error al cargar tags", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<List<Tag>>, t: Throwable) {
                Toast.makeText(this@ContentActivity, "Error de conexión al cargar tags", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
