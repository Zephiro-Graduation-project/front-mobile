package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.models.InventoryProduct

class Inventory_ItemAdapter(
    private val inventoryProductList: List<InventoryProduct>,
    private val onProductClick: (InventoryProduct) -> Unit
) : RecyclerView.Adapter<Inventory_ItemAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.img_product)
        val txtProductName: TextView = itemView.findViewById(R.id.txt_product_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_inventory_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = inventoryProductList[position]



        // Obtener el ID del recurso drawable usando el nombre de imagen
        val context = holder.itemView.context
        val imageResId = context.resources.getIdentifier(
            product.imageName.replace(".png", ""), // quitar extensión si es necesario
            "drawable",
            context.packageName
        )

        if (imageResId != 0) {
            holder.imgProduct.setImageResource(imageResId)
        } else {
            holder.imgProduct.setImageResource(R.drawable.logo_multimedia) // Imagen por defecto
        }

        holder.txtProductName.text = product.name


        // Click en la card para abrir el popup de detalles
        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount(): Int = inventoryProductList.size
}
