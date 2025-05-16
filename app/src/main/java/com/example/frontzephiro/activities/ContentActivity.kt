// ContentActivity.kt
package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.adapters.ContentAdapter
import com.example.frontzephiro.api.ContentApiService
import com.example.frontzephiro.api.ProfilingApiService
import com.example.frontzephiro.api.TagsApiService
import com.example.frontzephiro.models.Content
import com.example.frontzephiro.models.ProfileResponse
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContentActivity : AppCompatActivity() {

    private lateinit var chipGroup: ChipGroup
    private lateinit var rvContent: RecyclerView
    private lateinit var tvCategoryName: TextView
    private lateinit var contentAdapter: ContentAdapter

    private var selectedTagId: String? = null
    private var stressScore: Int = 0
    private var anxietyScore: Int = 0

    private val profilingService by lazy {
        RetrofitClient
            .getProfileClient()
            .create(ProfilingApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        findViewById<LottieAnimationView>(R.id.call).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@ContentActivity, EmergencyContactsActivity::class.java))
            }
        }
        findViewById<LottieAnimationView>(R.id.alert).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@ContentActivity, EmergencyNumbersActivity::class.java))
            }
        }

        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            selectedItemId = R.id.menuContenido
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menuInicio     -> startActivity(Intent(this@ContentActivity, HomeActivity::class.java))
                    R.id.menuSeguimiento-> startActivity(Intent(this@ContentActivity, TrackerMain::class.java))
                    R.id.menuJardin     -> startActivity(Intent(this@ContentActivity, GardenMain::class.java))
                    R.id.menuPerfil     -> startActivity(Intent(this@ContentActivity, ProfileActivity::class.java))
                    else                -> return@setOnItemSelectedListener false
                }
                true
            }
        }

        chipGroup      = findViewById(R.id.chipGroup)
        rvContent      = findViewById(R.id.rvContent)
        tvCategoryName = findViewById(R.id.namePersona)

        contentAdapter = ContentAdapter(emptyList()) { content ->
            startActivity(
                Intent(this@ContentActivity, SpecificContentActivity::class.java)
                    .putExtra("CONTENT_ID", content.id)
            )
        }
        rvContent.apply {
            layoutManager = LinearLayoutManager(this@ContentActivity)
            setHasFixedSize(true)
            adapter = contentAdapter
        }

        val userId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            .getString("USER_ID", "")
            .orEmpty()

        if (userId.isBlank()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            loadTags()
            loadContent()
            return
        }

        profilingService.getProfile(userId)
            .enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(call: Call<ProfileResponse>, resp: Response<ProfileResponse>) {
                    if (resp.isSuccessful && resp.body() != null) {
                        val profile = resp.body()!!
                        stressScore = profile.totalStressScore
                            ?: profile.stressIndicator.totalScore
                        anxietyScore = profile.totalAnxietyScore
                            ?: profile.anxietyIndicator.totalScore
                        Log.d("ContentActivity", "Perfil: stress=$stressScore, anxiety=$anxietyScore")
                    } else {
                        Log.e("ContentActivity", "Error perfil: ${resp.code()}")
                    }
                    loadTags()
                    if (stressScore > 0 || anxietyScore > 0) {
                        loadSuggestedContent(stressScore, anxietyScore)
                    } else {
                        loadContent()
                    }
                }
                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Log.e("ContentActivity", "Fallo red perfil", t)
                    loadTags()
                    loadContent()
                }
            })
    }

    private fun loadTags() {
        chipGroup.isSingleSelection = true
        RetrofitClient
            .getContentClient()
            .create(TagsApiService::class.java)
            .getAllTags()
            .enqueue(object : Callback<List<com.example.frontzephiro.models.Tag>> {
                override fun onResponse(
                    call: Call<List<com.example.frontzephiro.models.Tag>>,
                    resp: Response<List<com.example.frontzephiro.models.Tag>>
                ) {
                    if (!resp.isSuccessful) {
                        Toast.makeText(this@ContentActivity, "Error ${resp.code()} al cargar tags", Toast.LENGTH_SHORT).show()
                        return
                    }
                    chipGroup.removeAllViews()
                    resp.body().orEmpty().forEach { tag ->
                        // Creamos el Chip con el styleAttr de chipStyle para heredar colores
                        val chip = Chip(
                            chipGroup.context,
                            null,
                            com.google.android.material.R.attr.chipStyle
                        ).apply {
                            text = tag.name
                            isCheckable = true
                            setOnClickListener {
                                if (selectedTagId == tag.id) {
                                    selectedTagId = null
                                    chipGroup.clearCheck()
                                    tvCategoryName.text = getString(R.string.nombreCategoria)
                                    if (stressScore > 0 || anxietyScore > 0) {
                                        loadSuggestedContent(stressScore, anxietyScore)
                                    } else {
                                        loadContent()
                                    }
                                } else {
                                    selectedTagId = tag.id
                                    tvCategoryName.text = tag.name
                                    loadContentByTag(tag.id)
                                }
                            }
                        }
                        chipGroup.addView(chip)
                    }
                }
                override fun onFailure(call: Call<List<com.example.frontzephiro.models.Tag>>, t: Throwable) {
                    Toast.makeText(this@ContentActivity, "Fallo conexión tags", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadContent() {
        tvCategoryName.text = getString(R.string.nombreCategoria)
        RetrofitClient
            .getContentClient()
            .create(ContentApiService::class.java)
            .getAllContent()
            .enqueue(object : Callback<List<Content>> {
                override fun onResponse(call: Call<List<Content>>, resp: Response<List<Content>>) {
                    if (!resp.isSuccessful) {
                        Toast.makeText(this@ContentActivity, "Error al cargar contenido", Toast.LENGTH_SHORT).show()
                        return
                    }
                    contentAdapter.updateItems(resp.body().orEmpty())
                }
                override fun onFailure(call: Call<List<Content>>, t: Throwable) {
                    Toast.makeText(this@ContentActivity, "Error de conexión al cargar contenido", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadSuggestedContent(stress: Int, anxiety: Int) {
        tvCategoryName.text = "Recomendado para ti"
        RetrofitClient
            .getContentClient()
            .create(ContentApiService::class.java)
            .getSuggestedContent(stress, anxiety)
            .enqueue(object : Callback<List<Content>> {
                override fun onResponse(call: Call<List<Content>>, resp: Response<List<Content>>) {
                    if (!resp.isSuccessful) {
                        Toast.makeText(this@ContentActivity, "Error ${resp.code()} al cargar recomendaciones", Toast.LENGTH_SHORT).show()
                        return
                    }
                    contentAdapter.updateItems(resp.body().orEmpty())
                }
                override fun onFailure(call: Call<List<Content>>, t: Throwable) {
                    Toast.makeText(this@ContentActivity, "Fallo de red al cargar recomendaciones", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadContentByTag(tagId: String) {
        RetrofitClient
            .getContentClient()
            .create(ContentApiService::class.java)
            .getContentByTag(tagId)
            .enqueue(object : Callback<List<Content>> {
                override fun onResponse(call: Call<List<Content>>, resp: Response<List<Content>>) {
                    if (!resp.isSuccessful) {
                        Toast.makeText(this@ContentActivity, "Error al filtrar contenido", Toast.LENGTH_SHORT).show()
                        return
                    }
                    contentAdapter.updateItems(resp.body().orEmpty())
                }
                override fun onFailure(call: Call<List<Content>>, t: Throwable) {
                    Toast.makeText(this@ContentActivity, "Fallo al filtrar contenido", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
