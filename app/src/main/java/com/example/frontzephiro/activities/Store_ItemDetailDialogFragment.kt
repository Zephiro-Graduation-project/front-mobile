package com.example.frontzephiro.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import com.example.frontzephiro.models.Flower
import com.example.frontzephiro.models.StoreProduct

class Store_ItemDetailDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(storeProduct: StoreProduct): Store_ItemDetailDialogFragment {
            val fragment = Store_ItemDetailDialogFragment()
            val args = Bundle()
            args.putInt("productID", storeProduct.id)
            args.putString("productName", storeProduct.name)
            args.putInt("productPrice", storeProduct.price)
            args.putString("productImage", storeProduct.imageName)
            args.putString("productDescription", storeProduct.description)
            args.putString("productKind", storeProduct.kind)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val productName = requireArguments().getString("productName", "")
        val productPrice = requireArguments().getInt("productPrice", 0)
        val productImage = requireArguments().getString("productImage", "")
        val productDescription = requireArguments().getString("productDescription", "")
        val productKind = requireArguments().getString("productKind", "")
        val productID = requireArguments().getInt("productID", 0)


        val storeProduct = StoreProduct(productID,productName, productPrice, productImage, productDescription, productKind)

        return ComposeView(requireContext()).apply {
            setContent {
                StoreItemDetailDialog(
                    storeProduct = storeProduct,
                    onDismiss = { dismiss() }
                )
            }
        }
    }
}
