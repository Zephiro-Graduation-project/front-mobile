package com.example.frontzephiro.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment

class Inventory_ItemDetailDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(imageResId: Int, name: String, description: String): Inventory_ItemDetailDialogFragment {
            val fragment = Inventory_ItemDetailDialogFragment()
            val args = Bundle()
            args.putInt("imageResId", imageResId)
            args.putString("name", name)
            args.putString("description", description)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val imageResId = requireArguments().getInt("imageResId")
        val name = requireArguments().getString("name") ?: ""
        val description = requireArguments().getString("description") ?: ""

        return ComposeView(requireContext()).apply {
            setContent {
                InventoryItemDialog(
                    imageResId = imageResId,
                    name = name,
                    description = description,
                    onDismiss = { dismiss() }
                )
            }
        }
    }
}

