package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.adapters.Store_ItemAdapter
import com.example.frontzephiro.models.Store_Item
import com.google.android.material.bottomnavigation.BottomNavigationView

class GardenStore : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var storeItemAdapter: Store_ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garden_store)

        val callAnimation = findViewById<LottieAnimationView>(R.id.call)
        val alertAnimation = findViewById<LottieAnimationView>(R.id.alert)
        val backContainer = findViewById<LinearLayout>(R.id.backContainer)
        callAnimation.repeatCount = 0
        callAnimation.playAnimation()

        alertAnimation.repeatCount = 0
        alertAnimation.playAnimation()

        backContainer.setOnClickListener {
            onBackPressed()
        }

        callAnimation.setOnClickListener {
            val intent = Intent(this, EmergencyContactsActivity::class.java)
            startActivity(intent)
        }

        alertAnimation.setOnClickListener {
            val intent = Intent(this, EmergencyNumbersActivity::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recycler_tienda)

        // Lista de productos quemados
        val itemLists = listOf(
            Store_Item("Planta A", 20, R.drawable.planta_a, "Una hermosa planta decorativa.", "Plant"),
            Store_Item("Planta B", 35, R.drawable.planta_b, "Una planta con propiedades relajantes.", "Plant"),
            Store_Item("Planta C", 15, R.drawable.planta_c, "Una planta que purifica el aire.", "Plant"),
            Store_Item("Planta D", 10, R.drawable.planta_d, "Una planta que atrae la buena suerte.", "Plant"),
            Store_Item("japones", 50, R.drawable.japones, "Un ambiente al mejor estilo japones para relajarse.", "Background"),
            Store_Item("primavera", 0, R.drawable.primavera, "El ambiente inicial que todos los usuario reciben gratis.", "Background"),
            Store_Item("verano", 0, R.drawable.verano, "Un ambiente al mejor estilo japones para relajarse.", "Background"),
            Store_Item("invierno", 0, R.drawable.invierno, "El ambiente inicial que todos los usuario reciben gratis.", "Background"),
            Store_Item("magic", 0, R.drawable.magic, "Un ambiente al mejor estilo japones para relajarse.", "Background"),
            Store_Item("otono", 0, R.drawable.otono, "El ambiente inicial que todos los usuario reciben gratis.", "Background"),
            Store_Item("pasto", 0, R.drawable.pasto, "El ambiente inicial que todos los usuario reciben gratis.", "Background"),
        )

        // Configurar el RecyclerView con un GridLayoutManager de 2 filas
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        storeItemAdapter = Store_ItemAdapter(itemLists) { product ->
            showProductPopup(product) // Abre el popup al hacer clic
        }
        recyclerView.adapter = storeItemAdapter

        val plantas = itemLists.filter { it.kind == "Plant" }
        val fondos = itemLists.filter { it.kind == "Background" }

        val recyclerPlantas = findViewById<RecyclerView>(R.id.recycler_tienda)
        val recyclerFondos = findViewById<RecyclerView>(R.id.recycler_back_tienda)

        recyclerPlantas.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        recyclerFondos.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)

        recyclerPlantas.adapter = Store_ItemAdapter(plantas) { product ->
            showProductPopup(product)
        }
        recyclerFondos.adapter = Store_ItemAdapter(fondos) { product ->
            showProductPopup(product)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menuJardin
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
                /*
                R.id.menuJardin -> {
                    startActivity(Intent(this, GardenMain::class.java))
                    true
                } */
                R.id.menuContenido -> {
                    startActivity(Intent(this, ContentActivity::class.java))
                    true
                }
                R.id.menuPerfil -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // Función para mostrar el popup del producto
    private fun showProductPopup(storeItem: Store_Item) {
        val dialog = Store_ItemDetailDialogFragment.newInstance(storeItem)
        dialog.show(supportFragmentManager, "ProductDetailDialog")
    }
}