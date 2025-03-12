package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
// import com.bumptech.glide.Glide  //esto es lo de la imagen q lo dejo conectado por ahora
import com.example.frontzephiro.R
import com.example.frontzephiro.models.Content
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ContentAdapter(private val contentList: List<Content>) : RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_content, parent, false)
        return ContentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val content = contentList[position]
        holder.bind(content)
    }

    override fun getItemCount(): Int = contentList.size

    class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(content: Content) {
            itemView.findViewById<TextView>(R.id.tvTitle).text = content.name
            itemView.findViewById<TextView>(R.id.tvAuthor).text = content.author
            itemView.findViewById<TextView>(R.id.tvDescription).text = content.description

            // esto es lo de la imagen q lo dejo conectado por ahora
            /*
            Glide.with(itemView.context)
                .load(content.imagePath)
                .placeholder(R.drawable.placeholder_image)
                .into(itemView.findViewById(R.id.ivContentImage))
            */

            val chipGroup = itemView.findViewById<ChipGroup>(R.id.chipGroupItem)
            chipGroup.removeAllViews()
            content.tags.forEach { tag ->
                val chip = Chip(itemView.context)
                chip.text = tag.name
                chip.isClickable = false
                chip.isCheckable = false
                chipGroup.addView(chip)
            }
        }
    }
}
