package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.models.StoreProduct

class Store_ItemAdapter(
    private val storeProductList: List<StoreProduct>,
    private val onProductClick: (StoreProduct) -> Unit
) : RecyclerView.Adapter<Store_ItemAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.img_product)
        val txtProductName: TextView = itemView.findViewById(R.id.txt_product_name)
        val txtPrice: TextView = itemView.findViewById(R.id.txt_price)
        val lottieCoin: LottieAnimationView = itemView.findViewById(R.id.lottie_coin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_store_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = storeProductList[position]

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
        holder.txtPrice.text = "${product.price}$"

        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount(): Int = storeProductList.size
}
