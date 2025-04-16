package com.example.frontzephiro.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import com.example.frontzephiro.models.Store_Item

class Store_ItemDetailDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(storeItem: Store_Item): Store_ItemDetailDialogFragment {
            val fragment = Store_ItemDetailDialogFragment()
            val args = Bundle()
            args.putString("productName", storeItem.name)
            args.putInt("productPrice", storeItem.price)
            args.putInt("productImage", storeItem.imageResId)
            args.putString("productDescription", storeItem.description)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val productName = requireArguments().getString("productName", "")
        val productPrice = requireArguments().getInt("productPrice", 0)
        val productImage = requireArguments().getInt("productImage", 0)
        val productDescription = requireArguments().getString("productDescription", "")

        val storeItem = Store_Item(productName, productPrice, productImage, productDescription)

        return ComposeView(requireContext()).apply {
            setContent {
                StoreItemDetailDialog(
                    storeItem = storeItem,
                    onDismiss = { dismiss() }
                )
            }
        }
    }
}
