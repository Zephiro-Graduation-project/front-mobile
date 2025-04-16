package com.example.frontzephiro.activities

import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.View.DragShadowBuilder
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class GardenMain : AppCompatActivity() {

    private lateinit var gridJardin: GridLayout
    private val celdas = mutableListOf<FrameLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garden_main)

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
        val plantaResId = intent.getIntExtra("PLANTA_RES_ID", -1)
        val plantaNombre = intent.getStringExtra("PLANTA_NOMBRE") ?: "Planta desconocida"

        if (plantaResId != -1) {
            agregarElementoAlJardin(plantaResId, plantaNombre)
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
    private fun agregarElementoAlJardin(plantaResId: Int, plantaNombre: String) {
        if (!hayEspacioDisponible()) {
            Toast.makeText(this, "El jardín está lleno", Toast.LENGTH_SHORT).show()
            return
        }

        val planta = ImageView(this)
        planta.setImageResource(plantaResId)
        planta.tag = Pair(plantaResId, plantaNombre) // Guardamos imagen y nombre en tag

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

        for (celda in celdas) {
            if (celda.childCount == 0) {
                celda.addView(planta)
                return
            }
        }
    }

    private fun hayEspacioDisponible(): Boolean {
        return celdas.any { it.childCount == 0 }
    }

    private fun mostrarPopup(view: ImageView) {
        val plantaTag = view.tag as? Pair<Int, String>
        val imagenPlanta = plantaTag?.first ?: R.drawable.ic_launcher_foreground
        val nombrePlanta = plantaTag?.second ?: "Planta desconocida"

        val popup = PopupDialogFragment(
            imagenPlanta,
            nombrePlanta,
            onGuardarClicked = {
                (view.parent as? FrameLayout)?.removeView(view)
            }
        )
        popup.show(supportFragmentManager, "PopupDialogFragment")
    }

    private fun resaltarCeldas(resaltar: Boolean) {
        val color = if (resaltar) Color.parseColor("#88EEEEEE") else Color.TRANSPARENT
        celdas.forEach { it.setBackgroundColor(color) }
    }

}