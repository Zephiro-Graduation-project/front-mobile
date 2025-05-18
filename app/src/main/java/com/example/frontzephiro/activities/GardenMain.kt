package com.example.frontzephiro.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.View.DragShadowBuilder
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.example.frontzephiro.api.GamificationApiService
import com.example.frontzephiro.models.Background
import com.example.frontzephiro.models.Flower
import com.example.frontzephiro.models.GardenRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.frontzephiro.network.RetrofitClient
import com.example.frontzephiro.models.GardenResponse

class GardenMain : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var gridJardin: GridLayout
    private val celdas = mutableListOf<FrameLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garden_main)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        val userName = sharedPreferences.getString("USER_NAME", "Usuario")
        findViewById<TextView>(R.id.tvNombreJardin).text = "Jardín de: $userName"

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

        val backgroundView = findViewById<View>(R.id.background_view)
        val fondo = obtenerFondoSeleccionado(this)

        val fondoResId = when (fondo) {
            "primavera" -> R.drawable.primavera
            "verano" -> R.drawable.verano
            "invierno" -> R.drawable.invierno
            "japones" -> R.drawable.japones
            "magic" -> R.drawable.magico
            "otono" -> R.drawable.otono
            "pasto" -> R.drawable.pasto
            else -> R.drawable.primavera
        }

        backgroundView.setBackgroundResource(fondoResId)

        cargarJardinDesdeBackend()



        setupCardInteractions()

        gridJardin = findViewById(R.id.gridJardin)

        gridJardin.columnCount = 3
        gridJardin.rowCount = 4

        val totalCeldas = 12
        val cellSize = (resources.displayMetrics.widthPixels / 4) -43

        // Celdas dentro de la cuadrícula
        for (i in 0 until totalCeldas) {
            val celda = FrameLayout(this)
            celda.layoutParams = GridLayout.LayoutParams().apply {
                width = cellSize
                height = cellSize
                setMargins(11, 11, 11, 11)
                rowSpec = GridLayout.spec(i / 3)
                columnSpec = GridLayout.spec(i % 3)
            }
            celda.setBackgroundColor(Color.TRANSPARENT)
            gridJardin.addView(celda)
            celdas.add(celda)
        }

        // Recibir datos desde el inventario

        val flower: Flower? = intent.getParcelableExtra<Flower>("FLOWER")

        if (flower != null) {
            agregarElementoAlJardin(flower)
        }

        // Para arrastrar en la cuadrícula
        gridJardin.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_ENTERED -> resaltarCeldas(true)
                DragEvent.ACTION_DRAG_EXITED -> resaltarCeldas(false)
                DragEvent.ACTION_DROP -> {
                    val draggedView = event.localState as ImageView

                    val gridX = event.x.toInt()
                    val gridY = event.y.toInt()

                    val cellWidth = gridJardin.width / 3
                    val cellHeight = gridJardin.height / 4

                    val newColumn = gridX / cellWidth
                    val newRow = gridY / cellHeight

                    if (newRow in 0..3 && newColumn in 0..2) {
                        val targetCell = celdas[newRow * 3 + newColumn]
                        if (targetCell.childCount == 0) { // Solo mover si la celda está vacía
                            (draggedView.parent as? FrameLayout)?.removeView(draggedView)
                            targetCell.addView(draggedView)
                            actualizarJardinEnBackend()
                        } else {
                            Toast.makeText(this, "Espacio ocupado", Toast.LENGTH_SHORT).show()
                            draggedView.visibility = View.VISIBLE
                        }
                    } else {
                        draggedView.visibility = View.VISIBLE
                    }
                    resaltarCeldas(false)
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    val draggedView = event.localState as ImageView
                    if (draggedView.visibility == View.INVISIBLE) {
                        draggedView.visibility = View.VISIBLE
                    }
                    resaltarCeldas(false)
                }
            }
            true
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

    // Configuracion de botones de la pantalla principal
    private fun setupCardInteractions() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAlmacen).setOnClickListener {
            startActivity(Intent(this, GardenInventory::class.java))
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnTienda).setOnClickListener {
            startActivity(Intent(this, GardenStore::class.java))
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogros).setOnClickListener {
            startActivity(Intent(this, GardenAchievements::class.java))
        }
    }

    // Para agregar un nuevo elemento al jardín si hay espacio disponible
    private fun agregarElementoAlJardin(flower: Flower) {
        obtenerPrimeraCeldaDisponible { celdaIndex ->
            if (celdaIndex == null) {
                runOnUiThread {
                    Toast.makeText(this, "El jardín está lleno", Toast.LENGTH_SHORT).show()
                }
                return@obtenerPrimeraCeldaDisponible
            }

            runOnUiThread {
                val planta = ImageView(this)
                val resId = resources.getIdentifier(flower.healthyAsset, "drawable", packageName)
                planta.setImageResource(resId)
                planta.tag = flower

                planta.layoutParams = FrameLayout.LayoutParams(200, 200).apply {
                    setMargins(16, 16, 16, 16)
                }

                planta.setOnClickListener(object : View.OnClickListener {
                    private var lastClickTime: Long = 0
                    override fun onClick(v: View?) {
                        val clickTime = System.currentTimeMillis()
                        if (clickTime - lastClickTime < 300) {
                            mostrarPopup(planta)
                        }
                        lastClickTime = clickTime
                    }
                })

                planta.setOnLongClickListener { view ->
                    val clipData = ClipData.newPlainText("", "")
                    val shadowBuilder = View.DragShadowBuilder(view)
                    view.startDragAndDrop(clipData, shadowBuilder, view, 0)
                    view.visibility = View.INVISIBLE
                    resaltarCeldas(true)
                    true
                }

                if (celdaIndex in celdas.indices) {
                    val celda = celdas[celdaIndex]
                    celda.removeAllViews() // Por si acaso
                    celda.addView(planta)
                    actualizarJardinEnBackend()
                }
            }
        }
    }



    private fun hayEspacioDisponible(): Boolean {
        return celdas.any { celda ->
            celda.children.none { it is ImageView && it.visibility == View.VISIBLE }
        }
    }


    private fun mostrarPopup(view: ImageView) {
        val flower = view.tag as? Flower
        if (flower != null) {
            val resId = resources.getIdentifier(flower.healthyAsset.lowercase(), "drawable", packageName)
            val nombrePlanta = flower.name

            val popup = PopupDialogFragment(
                resId,
                nombrePlanta,
                onGuardarClicked = {
                    (view.parent as? FrameLayout)?.removeView(view)
                    actualizarJardinEnBackend()
                }
            )
            popup.show(supportFragmentManager, "PopupDialogFragment")

        }
    }

    private fun resaltarCeldas(resaltar: Boolean) {
        val color = if (resaltar) Color.parseColor("#88EEEEEE") else Color.TRANSPARENT
        celdas.forEach { it.setBackgroundColor(color) }
    }

    private fun obtenerFondoSeleccionado(context: Context): String {
        val prefs = context.getSharedPreferences("zephiro_prefs", Context.MODE_PRIVATE)
        return prefs.getString("fondo_jardin", "primavera") ?: "primavera" // Valor por defecto
    }

    @SuppressLint("SuspiciousIndentation")
    private fun cargarJardinDesdeBackend() {
    val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
    val userId = prefs.getString("USER_ID", null) ?: return

    val service = RetrofitClient.getAuthenticatedGamificationClient(this)
        .create(GamificationApiService::class.java)

        service.getUserGarden(userId).enqueue(object : Callback<GardenResponse> {
            override fun onResponse(call: Call<GardenResponse>, response: Response<GardenResponse>) {
                if (response.isSuccessful) {
                    val jardin = response.body() ?: return

                    // Renderizar flores en el grid
                    jardin.flowers.forEachIndexed { index, flower ->
                        flower?.let {
                            val planta = ImageView(this@GardenMain)
                            if (jardin.state == true) {
                                val resId = resources.getIdentifier(
                                    it.healthyAsset.lowercase(), "drawable", packageName
                                )
                                planta.setImageResource(resId)
                                planta.tag = it
                            } else {
                                val resId = resources.getIdentifier(
                                    it.dryAsset.lowercase(), "drawable", packageName
                                )
                                planta.setImageResource(resId)
                                planta.tag = it
                            }


                            planta.layoutParams = FrameLayout.LayoutParams(200, 200).apply {
                                setMargins(16, 16, 16, 16)
                            }

                            planta.setOnClickListener(object : View.OnClickListener {
                                private var lastClickTime: Long = 0
                                override fun onClick(v: View?) {
                                    val clickTime = System.currentTimeMillis()
                                    if (clickTime - lastClickTime < 300) {
                                        mostrarPopup(planta)
                                    }
                                    lastClickTime = clickTime
                                }
                            })

                            planta.setOnLongClickListener { view ->
                                val clipData = ClipData.newPlainText("", "")
                                val shadowBuilder = DragShadowBuilder(view)
                                view.startDragAndDrop(clipData, shadowBuilder, view, 0)
                                view.visibility = View.INVISIBLE
                                resaltarCeldas(true)
                                true
                            }

                            celdas.getOrNull(index)?.apply {
                                if (childCount == 0) addView(planta)
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@GardenMain, "Error al cargar el jardín", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GardenResponse>, t: Throwable) {
                Log.e("GardenMain", "Fallo de conexión:")
                //Toast.makeText(this@GardenMain, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarJardinEnBackend() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", null)

        if (userId == null) {
            Log.e("GardenMain", "Usuario no identificado")
            //Toast.makeText(this, "Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        val fondoSeleccionado = obtenerFondoSeleccionado(this)

        val fondoCompleto = when (fondoSeleccionado) {
            "primavera" -> Background(1, "Primavera", "Un jardín florecido, lleno de vitalidad y renovación.", 200)
            "verano" -> Background(2, "Verano", "Radiante y lleno de luz, evoca la alegría del sol y la vida al aire libre.", 200)
            "invierno" -> Background(3, "Invierno", "Sereno y blanco, representa el descanso y la introspección.", 200)
            "japones" -> Background(4, "Japones", "Minimalismo y belleza natural, paz en cada detalle.", 200)
            "magic" -> Background(5, "Magic", "Un rincón encantado, donde la fantasía florece en cada hoja.", 200)
            "otono" -> Background(6, "Otono", "Hojas caídas y colores cálidos, el jardín se prepara para dormir.", 200)
            "pasto" -> Background(7, "Pasto", "Sencillo y verde, un lienzo perfecto para nuevas semillas.", 200)
            else -> Background(1, "Primavera", "Un jardín florecido, lleno de vitalidad y renovación.", 200)
        }

        val flores = List(12) { i ->
            val celda = celdas[i]
            if (celda.childCount > 0) {
                val planta = celda.getChildAt(0) as? ImageView
                planta?.tag as? Flower

            } else null
        }

        val gardenRequest = GardenRequest(
            background = fondoCompleto,
            flowers = flores
        )

        val service = RetrofitClient.getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)

        service.updateGarden(userId, gardenRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.e("GardenMain", "Jardín actualizado")
                    //Toast.makeText(this@GardenMain, "Jardín actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("GardenMain", "Error al actualizar jardín")
                    //Toast.makeText(this@GardenMain, "Error al actualizar jardín", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("GardenMain", "Fallo de red al actualizar")
                //Toast.makeText(this@GardenMain, "Fallo de red al actualizar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun obtenerPrimeraCeldaDisponible(onResultado: (Int?) -> Unit) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", null) ?: run {
            onResultado(null)
            return
        }

        val service = RetrofitClient.getAuthenticatedGamificationClient(this)
            .create(GamificationApiService::class.java)

        service.getUserGarden(userId).enqueue(object : Callback<GardenResponse> {
            override fun onResponse(call: Call<GardenResponse>, response: Response<GardenResponse>) {
                if (response.isSuccessful) {
                    val jardin = response.body() ?: run {
                        onResultado(null)
                        return
                    }

                    val primeraCeldaDisponible = jardin.flowers.indexOfFirst { it == null }
                    onResultado(if (primeraCeldaDisponible != -1) primeraCeldaDisponible else null)
                } else {
                    onResultado(null)
                }
            }

            override fun onFailure(call: Call<GardenResponse>, t: Throwable) {
                onResultado(null)
            }
        })
    }

    private fun setBackgroundFromTitle(title: String) {
        val backgroundView = findViewById<View>(R.id.background_view)

        val fondoResId = when (title.lowercase()) {
            "primavera" -> R.drawable.primavera
            "verano" -> R.drawable.verano
            "invierno" -> R.drawable.invierno
            "japones" -> R.drawable.japones
            "magic", "mágico", "magico" -> R.drawable.magico
            "otono", "otoño" -> R.drawable.otono
            "pasto" -> R.drawable.pasto
            else -> R.drawable.primavera // Fondo por defecto si no se reconoce el título
        }

        backgroundView.setBackgroundResource(fondoResId)

        // También puedes guardar esta elección en SharedPreferences si quieres mantenerla
        val prefs = getSharedPreferences("zephiro_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fondo_jardin", title.lowercase()).apply()
    }


}