package com.example.frontzephiro.activities

import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.View.DragShadowBuilder
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.frontzephiro.R

class GardenMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garden_main)

        setupCardInteractions()

        val gridJardin = findViewById<GridLayout>(R.id.gridJardin)

        for (i in 0 until 9) {
            val planta = ImageView(this)
            planta.setImageResource(R.drawable.basura)
            planta.layoutParams = GridLayout.LayoutParams().apply {
                width = 100
                height = 100
                setMargins(8, 8, 8, 8)
            }
            planta.setOnLongClickListener { view ->
                val clipData = ClipData.newPlainText("", "")
                val shadowBuilder = DragShadowBuilder(view)
                view.startDragAndDrop(clipData, shadowBuilder, view, 0)
                view.visibility = View.INVISIBLE
                true
            }
            gridJardin.addView(planta)
        }

        gridJardin.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_ENTERED -> {
                    gridJardin.setBackgroundColor(Color.LTGRAY)
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    gridJardin.setBackgroundColor(Color.TRANSPARENT)
                }
                DragEvent.ACTION_DROP -> {
                    val draggedView = event.localState as View
                    val newParams = GridLayout.LayoutParams().apply {
                        width = 100
                        height = 100
                        setMargins(8, 8, 8, 8)
                        rowSpec = GridLayout.spec(GridLayout.UNDEFINED)
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED)
                    }
                    draggedView.layoutParams = newParams
                    draggedView.visibility = View.VISIBLE
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    gridJardin.setBackgroundColor(Color.TRANSPARENT)
                }
            }
            true
        }
    }

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
}
