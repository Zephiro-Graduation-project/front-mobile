package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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
        val itemLists = listOf(
            Inventory_Item("Planta A", R.drawable.planta_a, "Una hermosa planta decorativa.", "Plant"),
            Inventory_Item("Planta B", R.drawable.planta_b, "Una planta con propiedades relajantes.", "Plant"),
            Inventory_Item("Planta C", R.drawable.planta_c, "Una planta que purifica el aire.", "Plant"),
            Inventory_Item("Planta D", R.drawable.planta_d, "Una planta que atrae la buena suerte.", "Plant"),
            Inventory_Item("Planta A", R.drawable.planta_a, "Una hermosa planta decorativa.", "Plant"),
            Inventory_Item("Planta B", R.drawable.planta_b, "Una planta con propiedades relajantes.", "Plant"),
            Inventory_Item("Planta C", R.drawable.planta_c, "Una planta que purifica el aire.", "Plant"),
            Inventory_Item("Planta D", R.drawable.planta_d, "Una planta que atrae la buena suerte.", "Plant"),
            Inventory_Item("japones", R.drawable.japones, "Un ambiente al mejor estilo japones para relajarse.", "Background"),
            Inventory_Item("primavera", R.drawable.primavera, "El ambiente inicial que todos los usuario reciben gratis.", "Background"),
            Inventory_Item("verano", R.drawable.verano, "Un ambiente al mejor estilo japones para relajarse.", "Background"),
            Inventory_Item("invierno", R.drawable.invierno, "El ambiente inicial que todos los usuario reciben gratis.", "Background"),
            Inventory_Item("magic", R.drawable.magic, "Un ambiente al mejor estilo japones para relajarse.", "Background"),
            Inventory_Item("otono", R.drawable.otono, "El ambiente inicial que todos los usuario reciben gratis.", "Background"),
            Inventory_Item("pasto", R.drawable.pasto, "El ambiente inicial que todos los usuario reciben gratis.", "Background"),

        )

        // Configuracion del RecyclerView con un GridLayoutManager de 2 filas
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        inventoryItemAdapter = Inventory_ItemAdapter(itemLists) { product ->
            showProductPopup(product) // Para abrir el popup al hacer clic
        }
        recyclerView.adapter = inventoryItemAdapter


        val plantas = itemLists.filter { it.kind == "Plant" }
        val fondos = itemLists.filter { it.kind == "Background" }

        val recyclerPlantas = findViewById<RecyclerView>(R.id.recycler_almacen)
        val recyclerFondos = findViewById<RecyclerView>(R.id.recycler_back_almacen)

        recyclerPlantas.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        recyclerFondos.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)

        recyclerPlantas.adapter = Inventory_ItemAdapter(plantas) { product ->
            showProductPopup(product)
        }
        recyclerFondos.adapter = Inventory_ItemAdapter(fondos) { product ->
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
    private fun showProductPopup(inventoryItem: Inventory_Item) {
        val dialog = Inventory_ItemDetailDialogFragment.newInstance(
            inventoryItem.imageResId,
            inventoryItem.name,
            inventoryItem.description,
            inventoryItem.kind
        )
        dialog.show(supportFragmentManager, "InventoryComposeDialog")
    }
}