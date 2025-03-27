package com.example.frontzephiro.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.frontzephiro.R
import com.example.frontzephiro.models.Store_Item

class Store_ItemDetailDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_PRODUCT_NAME = "product_name"
        private const val ARG_PRODUCT_PRICE = "product_price"
        private const val ARG_PRODUCT_IMAGE = "product_image"
        private const val ARG_PRODUCT_DESCRIPTION = "product_description"

        fun newInstance(storeItem: Store_Item): Store_ItemDetailDialogFragment {
            val fragment = Store_ItemDetailDialogFragment()
            val args = Bundle()
            args.putString(ARG_PRODUCT_NAME, storeItem.name)
            args.putInt(ARG_PRODUCT_PRICE, storeItem.price)
            args.putInt(ARG_PRODUCT_IMAGE, storeItem.imageResId) // To do: Ver como se va a recibir la imagen
            args.putString(ARG_PRODUCT_DESCRIPTION, storeItem.description)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_store_item_detail, null)

        val imgProduct = view.findViewById<ImageView>(R.id.img_product_detail)
        val txtProductName = view.findViewById<TextView>(R.id.txt_product_name_detail)
        val txtProductPrice = view.findViewById<TextView>(R.id.txt_product_price_detail)
        val txtProductDescription = view.findViewById<TextView>(R.id.txt_product_description_detail)
        val btnBuy = view.findViewById<Button>(R.id.btn_buy)

        // Obtener datos del producto enviados al DialogFragment
        val name = arguments?.getString(ARG_PRODUCT_NAME) ?: ""
        val price = arguments?.getInt(ARG_PRODUCT_PRICE) ?: 0
        val imageResId = arguments?.getInt(ARG_PRODUCT_IMAGE) ?: 0
        val description = arguments?.getString(ARG_PRODUCT_DESCRIPTION) ?: ""

        // Setear la información en los elementos del popup
        imgProduct.setImageResource(imageResId)
        txtProductName.text = name
        txtProductPrice.text = "$${price}"
        txtProductDescription.text = description

        // Acción del botón "Comprar"
        btnBuy.setOnClickListener {
            // Aquí podrías agregar la lógica de compra
            val intent = Intent(requireContext(), GardenMain::class.java)
            dismiss() // Cerrar el popup
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(true) // Permite cerrar tocando fuera del diálogo
            .create()
    }
}