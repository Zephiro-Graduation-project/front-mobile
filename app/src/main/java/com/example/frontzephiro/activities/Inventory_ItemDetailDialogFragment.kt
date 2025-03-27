package com.example.frontzephiro.activities

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
import com.example.frontzephiro.models.Inventory_Item

class Inventory_ItemDetailDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_PRODUCT_NAME = "product_name"
        private const val ARG_PRODUCT_IMAGE = "product_image"
        private const val ARG_PRODUCT_DESCRIPTION = "product_description"

        fun newInstance(inventoryItem: Inventory_Item): Inventory_ItemDetailDialogFragment {
            val fragment = Inventory_ItemDetailDialogFragment()
            val args = Bundle()
            args.putString(ARG_PRODUCT_NAME, inventoryItem.name)
            args.putInt(ARG_PRODUCT_IMAGE, inventoryItem.imageResId)
            args.putString(ARG_PRODUCT_DESCRIPTION, inventoryItem.description)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_inventory_item_detail, null)

        val imgProduct = view.findViewById<ImageView>(R.id.img_product_detail)
        val txtProductName = view.findViewById<TextView>(R.id.txt_product_name_detail)
        val txtProductDescription = view.findViewById<TextView>(R.id.txt_product_description_detail)
        val btnSembrar = view.findViewById<Button>(R.id.btn_sembrar)

        val name = arguments?.getString(ARG_PRODUCT_NAME) ?: ""
        val imageResId = arguments?.getInt(ARG_PRODUCT_IMAGE) ?: 0
        val description = arguments?.getString(ARG_PRODUCT_DESCRIPTION) ?: ""

        imgProduct.setImageResource(imageResId)
        txtProductName.text = name
        txtProductDescription.text = description

        btnSembrar.setOnClickListener {
            val intent = Intent(requireContext(), GardenMain::class.java)
            intent.putExtra("PLANTA_RES_ID", imageResId)
            intent.putExtra("PLANTA_NOMBRE", name)
            startActivity(intent)
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(true)
            .create()
    }
}
