// QuestionariesAdapter.kt
package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.frontzephiro.databinding.ItemShowQuestionariesCardBinding
import com.example.frontzephiro.models.QuestionnaireSummary

class QuestionariesAdapter(
    private var items: List<QuestionnaireSummary>,
    private val onItemClick: (QuestionnaireSummary) -> Unit
) : RecyclerView.Adapter<QuestionariesAdapter.ViewHolder>() {

    fun updateItems(newItems: List<QuestionnaireSummary>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShowQuestionariesCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val b: ItemShowQuestionariesCardBinding)
        : RecyclerView.ViewHolder(b.root) {

        fun bind(summary: QuestionnaireSummary) {
            b.preguntaGeneral.text = summary.name
            // ¡Registro del click aquí!
            b.root.setOnClickListener {
                onItemClick(summary)
            }
        }
    }
}
