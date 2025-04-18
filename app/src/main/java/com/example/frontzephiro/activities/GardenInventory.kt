package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.adapters.Inventory_ItemAdapter
import com.example.frontzephiro.models.Inventory_Item
import com.google.android.material.bottomnavigation.BottomNavigationView

class GardenInventory : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var inventoryItemAdapter: Inventory_ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garden_inventory)

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

        recyclerView = findViewById(R.id.recycler_almacen)

        // Productos del inventario quemados To do: Ver como se van a recibir los productos
        val storeItemLists = listOf(
            Inventory_Item("Planta A", R.drawable.planta_a, "Una hermosa planta decorativa."),
            Inventory_Item("Planta B", R.drawable.planta_b, "Una planta con propiedades relajantes."),
            Inventory_Item("Planta C", R.drawable.planta_c, "Una planta que purifica el aire."),
            Inventory_Item("Planta D", R.drawable.planta_d, "Una planta que atrae la buena suerte."),
        )

        // Configuracion del RecyclerView con un GridLayoutManager de 3 columnas
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        inventoryItemAdapter = Inventory_ItemAdapter(storeItemLists) { product ->
            showProductPopup(product) // Para abrir el popup al hacer clic
        }
        recyclerView.adapter = inventoryItemAdapter

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
    private fun showProductPopup(inventoryItem: Inventory_Item) {
        val dialog = Inventory_ItemDetailDialogFragment.newInstance(
            inventoryItem.imageResId,
            inventoryItem.name,
            inventoryItem.description
        )
        dialog.show(supportFragmentManager, "InventoryComposeDialog")
    }
}