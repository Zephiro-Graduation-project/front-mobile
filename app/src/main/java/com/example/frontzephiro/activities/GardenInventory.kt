package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.adapters.Inventory_ItemAdapter
import com.example.frontzephiro.adapters.Store_ItemAdapter
import com.example.frontzephiro.api.GamificationApiService
import com.example.frontzephiro.models.Background
import com.example.frontzephiro.models.Flower
import com.example.frontzephiro.models.InventoryProduct
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.Normalizer

class GardenInventory : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var inventoryItemAdapter: Inventory_ItemAdapter

    private lateinit var recyclerViewP: RecyclerView
    private lateinit var recyclerViewB: RecyclerView
    private lateinit var plantAdapter: Inventory_ItemAdapter
    private lateinit var backgroundAdapter: Inventory_ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garden_inventory)

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

        recyclerViewP = findViewById(R.id.recycler_almacen)

        loadFlowers { flowers ->
            // Convertir la lista de flores a una lista de InventoryProduct
            val inventoryPlants = flowers.map { flower ->
                InventoryProduct(
                    name = flower.name,
                    imageName = flower.healthyAsset,  // Usamos healthyAsset para la imagen
                    description = flower.description,
                    kind = "Plant"  // Todos estos elementos son plantas
                )
            }

            // Ahora puedes usar inventoryPlants como desees, por ejemplo:
            recyclerViewP.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)

            plantAdapter = Inventory_ItemAdapter(inventoryPlants) { product ->
                showProductPopup(product)
            }
            recyclerViewP.adapter = plantAdapter
        }

        recyclerViewB = findViewById(R.id.recycler_back_almacen)

        loadBackgrounds { backgrounds ->
            // Convertir la lista de fondos a una lista de InventoryProduct
            val inventoryBackgrounds = backgrounds.map { background ->
                InventoryProduct(
                    name = background.title,
                    imageName = normalizarTexto(background.title),  // Usamos title para la imagen
                    description = background.description,
                    kind = "Background"  // Todos estos elementos son fondos
                )
            }

            // Ahora puedes usar inventoryBackgrounds como desees, por ejemplo:
            recyclerViewB.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)

            backgroundAdapter = Inventory_ItemAdapter(inventoryBackgrounds) { product ->
                showProductPopup(product)
            }
            recyclerViewB.adapter = backgroundAdapter
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
    private fun showProductPopup(inventoryProduct: InventoryProduct) {
        val dialog = Inventory_ItemDetailDialogFragment.newInstance(inventoryProduct)
        dialog.show(supportFragmentManager, "InventoryComposeDialog")
    }

    private fun loadFlowers(onFlowersLoaded: (List<Flower>) -> Unit) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", null)

        if (userId == null) {
            Log.e("GardenInventory", "USER_ID no encontrado en SharedPreferences")
            Toast.makeText(this, "No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("GardenInventory", "Cargando flores con USER_ID: $userId")

        val flowersApi = RetrofitClient.getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)
        val call = flowersApi.getInventoryFlowers(userId)

        call.enqueue(object : retrofit2.Callback<List<Flower>> {
            override fun onResponse(
                call: retrofit2.Call<List<Flower>>,
                response: retrofit2.Response<List<Flower>>
            ) {
                if (response.isSuccessful) {
                    val flowers = response.body() ?: emptyList()
                    Log.d("GardenInventory", "Se recibieron ${flowers.size} flores del inventario")
                    onFlowersLoaded(flowers)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("GardenInventory", "Error al cargar flores: $errorBody")
                    Toast.makeText(this@GardenInventory, "Error al cargar flores del inventario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Flower>>, t: Throwable) {
                Log.e("GardenInventory", "Fallo de conexión al cargar flores", t)
                Toast.makeText(this@GardenInventory, "Error de conexión al cargar flores", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadBackgrounds(onBackgroundsLoaded: (List<Background>) -> Unit) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", null)

        if (userId == null) {
            Log.e("GardenInventory", "USER_ID no encontrado en SharedPreferences")
            Toast.makeText(this, "No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("GardenInventory", "Cargando fondos con USER_ID: $userId")

        val backgroundsApi = RetrofitClient.getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)
        val call = backgroundsApi.getInventoryBackgrounds(userId)

        call.enqueue(object : retrofit2.Callback<List<Background>> {
            override fun onResponse(
                call: retrofit2.Call<List<Background>>,
                response: retrofit2.Response<List<Background>>
            ) {
                if (response.isSuccessful) {
                    val backgrounds = response.body() ?: emptyList()
                    Log.d("GardenInventory", "Se recibieron ${backgrounds.size} fondos del inventario")
                    onBackgroundsLoaded(backgrounds)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("GardenInventory", "Error al cargar fondos: $errorBody")
                    Toast.makeText(this@GardenInventory, "Error al cargar fondos del inventario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Background>>, t: Throwable) {
                Log.e("GardenInventory", "Fallo de conexión al cargar fondos", t)
                Toast.makeText(this@GardenInventory, "Error de conexión al cargar fondos", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun normalizarTexto(texto: String): String {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "") // Quita tildes
            .replace('ñ', 'n') // Reemplaza ñ minúscula
            .replace('Ñ', 'n') // Reemplaza Ñ mayúscula también por minúscula n
            .lowercase() // Convierte a minúsculas
    }
}