package com.example.frontzephiro.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment

class Inventory_ItemDetailDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(imageResId: Int, name: String, description: String, kind: String): Inventory_ItemDetailDialogFragment {
            val args = Bundle().apply {
                putInt("imageResId", imageResId)
                putString("name", name)
                putString("description", description)
                putString("kind", kind)
            }
            val fragment = Inventory_ItemDetailDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val imageResId = arguments?.getInt("imageResId") ?: 0
        val name = arguments?.getString("name") ?: ""
        val description = arguments?.getString("description") ?: ""
        val kind = arguments?.getString("kind") ?: "Plant" // Valor por defecto

        return ComposeView(requireContext()).apply {
            setContent {
                InventoryItemDialog(
                    imageResId = imageResId,
                    name = name,
                    description = description,
                    kind = kind,
                    onDismiss = { dismiss() }
                )
            }
        }
    }
}
