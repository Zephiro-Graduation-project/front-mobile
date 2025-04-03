package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.frontzephiro.R
import com.example.frontzephiro.models.Store_Item

class Store_ItemAdapter(
    private val storeItemList: List<Store_Item>,
    private val onProductClick: (Store_Item) -> Unit
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
        val product = storeItemList[position]

        Glide.with(holder.itemView.context)
            .load(product.imageResId)
            .override(200, 200)
            .into(holder.imgProduct)

        holder.txtProductName.text = product.name
        holder.txtPrice.text = "${product.price}$"

        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount(): Int = storeItemList.size
}
