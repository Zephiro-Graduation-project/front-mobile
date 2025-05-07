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
import com.example.frontzephiro.api.TagsApiService
import com.example.frontzephiro.models.Content
import com.example.frontzephiro.models.Tag
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        // Lottie buttons
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

        // Bottom nav
        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            selectedItemId = R.id.menuContenido
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menuInicio    -> startActivity(Intent(this@ContentActivity, HomeActivity::class.java))
                    R.id.menuSeguimiento-> startActivity(Intent(this@ContentActivity, TrackerMain::class.java))
                    R.id.menuJardin    -> startActivity(Intent(this@ContentActivity, GardenMain::class.java))
                    R.id.menuPerfil    -> startActivity(Intent(this@ContentActivity, ProfileActivity::class.java))
                    else               -> return@setOnItemSelectedListener false
                }
                true
            }
        }

        // UI refs
        chipGroup       = findViewById(R.id.chipGroup)
        rvContent       = findViewById(R.id.rvContent)
        tvCategoryName  = findViewById(R.id.namePersona)

        // Configuramos singleSelection correctamente:
        chipGroup.isSingleSelection = true

        // Recycler + Adapter, ahora con callback al click:
        contentAdapter = ContentAdapter(emptyList()) { content ->
            // Al tocar una card lanzamos SpecificContentActivity con el ID
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

        loadTags()
        loadContent()
    }

    private fun loadTags() {
        RetrofitClient
            .getContentClient()
            .create(TagsApiService::class.java)
            .getAllTags()
            .enqueue(object : Callback<List<Tag>> {
                override fun onResponse(call: Call<List<Tag>>, resp: Response<List<Tag>>) {
                    if (!resp.isSuccessful) {
                        Log.e("ContentActivity", "Error ${resp.code()} al cargar tags")
                        return
                    }
                    chipGroup.removeAllViews()
                    resp.body().orEmpty().forEach { tag ->
                        Chip(this@ContentActivity).apply {
                            text        = tag.name
                            isCheckable = true
                            setOnClickListener {
                                if (selectedTagId == tag.id) {
                                    // misma etiqueta: deseleccionamos y volvemos a todo
                                    selectedTagId = null
                                    chipGroup.clearCheck()
                                    tvCategoryName.text = getString(R.string.nombreCategoria)
                                    loadContent()
                                } else {
                                    // nueva etiqueta: filtramos
                                    selectedTagId = tag.id
                                    tvCategoryName.text = tag.name
                                    loadContentByTag(tag.id)
                                }
                            }
                            chipGroup.addView(this)
                        }
                    }
                }

                override fun onFailure(call: Call<List<Tag>>, t: Throwable) {
                    Log.e("ContentActivity", "Fallo conexión tags", t)
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
                        Toast.makeText(
                            this@ContentActivity,
                            "Error al cargar contenido",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    contentAdapter.updateItems(resp.body().orEmpty())
                }

                override fun onFailure(call: Call<List<Content>>, t: Throwable) {
                    Toast.makeText(
                        this@ContentActivity,
                        "Error de conexión al cargar contenido",
                        Toast.LENGTH_SHORT
                    ).show()
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
                        Toast.makeText(
                            this@ContentActivity,
                            "Error al filtrar contenido",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    contentAdapter.updateItems(resp.body().orEmpty())
                }

                override fun onFailure(call: Call<List<Content>>, t: Throwable) {
                    Toast.makeText(
                        this@ContentActivity,
                        "Fallo al filtrar contenido",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
