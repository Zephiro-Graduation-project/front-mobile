// adapters/ResponseAdapter.kt
package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.frontzephiro.databinding.ItemResponseCardBinding
import com.example.frontzephiro.models.ResponseItem

class ResponseAdapter(
    private var items: List<ResponseItem>
) : RecyclerView.Adapter<ResponseAdapter.ViewHolder>() {

    fun updateItems(newItems: List<ResponseItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResponseCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(items[pos])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val b: ItemResponseCardBinding)
        : RecyclerView.ViewHolder(b.root) {
        fun bind(resp: ResponseItem) {
            b.preguntaGeneral.text    = resp.question
            b.textoGeneral.text       = resp.selectedAnswer
        }
    }
}
