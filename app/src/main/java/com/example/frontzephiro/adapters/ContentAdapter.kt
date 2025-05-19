// adapters/ContentAdapter.kt
package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.frontzephiro.R
import com.example.frontzephiro.models.Content
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ContentAdapter(
    private var contentList: List<Content>,
    private val onItemClick: (Content) -> Unit
) : RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

    fun updateItems(newList: List<Content>) {
        contentList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content, parent, false)
        return ContentViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(contentList[position])
    }

    override fun getItemCount(): Int = contentList.size

    class ContentViewHolder(
        itemView: View,
        private val onItemClick: (Content) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle       = itemView.findViewById<TextView>(R.id.tvTitle)
        private val tvAuthor      = itemView.findViewById<TextView>(R.id.tvAuthor)
        private val tvDescription = itemView.findViewById<TextView>(R.id.tvDescription)
        private val chipGroup     = itemView.findViewById<ChipGroup>(R.id.chipGroupItem)

        fun bind(content: Content) {
            tvTitle.text       = content.name
            tvAuthor.text      = content.author
            tvDescription.text = content.description

            chipGroup.removeAllViews()
            content.tags.forEach { tag ->
                Chip(itemView.context).apply {
                    text        = tag.name
                    isClickable = false
                    isCheckable = false
                    chipGroup.addView(this)
                }
            }

            itemView.setOnClickListener {
                onItemClick(content)
            }
        }
    }
}
