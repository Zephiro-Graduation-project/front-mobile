package com.example.frontzephiro.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.frontzephiro.R

class PopupDialogFragment(
    private val imagenResId: Int, // ID del recurso de imagen
    private val descripcion: String,
    private val onGuardarClicked: () -> Unit // Callback cuando se presione guardar
) : DialogFragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_garden_popup, container, false)

        val imageView = view.findViewById<ImageView>(R.id.ivElemento)
        val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcion)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)

        imageView.setImageResource(imagenResId)
        tvDescripcion.text = descripcion

        btnGuardar.setOnClickListener {
            onGuardarClicked()
            dismiss() // Cierra el popup
        }

        return view
    }
}
