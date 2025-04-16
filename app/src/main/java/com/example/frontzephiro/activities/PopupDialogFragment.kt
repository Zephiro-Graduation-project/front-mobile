package com.example.frontzephiro.activities

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView

class PopupDialogFragment(
    private val imagenResId: Int,
    private val descripcion: String,
    private val onGuardarClicked: () -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    GardenMainDialog(
                        onDismissRequest = { dismiss() },
                        onGuardarClicked = {
                            onGuardarClicked()
                            dismiss()
                        },
                        imageResId = imagenResId,
                        descripcion = descripcion
                    )
                }
            }
        }
    }
}

