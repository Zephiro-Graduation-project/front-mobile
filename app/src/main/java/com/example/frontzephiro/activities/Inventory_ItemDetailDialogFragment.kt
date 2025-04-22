package com.example.frontzephiro.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import com.example.frontzephiro.models.InventoryProduct

class Inventory_ItemDetailDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(inventoryProduct: InventoryProduct): Inventory_ItemDetailDialogFragment {
            val fragment = Inventory_ItemDetailDialogFragment()
            val args = Bundle()
            args.putString("productName", inventoryProduct.name)
            args.putString("productImage", inventoryProduct.imageName)
            args.putString("productDescription", inventoryProduct.description)
            args.putString("productKind", inventoryProduct.kind)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val productName = requireArguments().getString("productName", "")
        val productImage = requireArguments().getString("productImage", "")
        val productDescription = requireArguments().getString("productDescription", "")
        val productKind = requireArguments().getString("productKind", "")

        val inventoryProduct = InventoryProduct(productName, productImage, productDescription, productKind)

        return ComposeView(requireContext()).apply {
            setContent {
                InventoryItemDialog(
                    inventoryProduct = inventoryProduct,
                    onDismiss = { dismiss() }
                )
            }
        }
    }
}
