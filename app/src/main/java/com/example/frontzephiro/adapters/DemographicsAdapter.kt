package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.frontzephiro.databinding.ItemQuestionCardDemographicsBinding
import com.example.frontzephiro.models.Question
import com.example.frontzephiro.models.ResponseItem

class DemographicsAdapter(
    private var questions: List<Question>
) : RecyclerView.Adapter<DemographicsAdapter.ViewHolder>() {

    private val responseItems: MutableList<ResponseItem> = questions
        .map { q ->
            ResponseItem(
                question       = q.text,
                selectedAnswer = "",
                numericalValue = 0,
                measures       = q.measures
            )
        }
        .toMutableList()

    fun getResponses(): List<ResponseItem> = responseItems

    fun updateQuestions(newQuestions: List<Question>) {
        questions = newQuestions
        responseItems.clear()
        responseItems.addAll(newQuestions.map { q ->
            ResponseItem(
                question       = q.text,
                selectedAnswer = "",
                numericalValue = 0,
                measures       = q.measures
            )
        })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuestionCardDemographicsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    override fun getItemCount(): Int = questions.size

    inner class ViewHolder(
        private val b: ItemQuestionCardDemographicsBinding
    ) : RecyclerView.ViewHolder(b.root) {

        fun bind(q: Question, position: Int) {
            b.preguntaGeneral.text = q.text

            b.textoGeneral.text = q.measures.firstOrNull()
                ?.replace('_', ' ')
                ?.capitalize() ?: ""

            val optionLabels = q.answers.map { it.text }
            val adapter = ArrayAdapter(
                b.etDropdown.context,
                android.R.layout.simple_list_item_1,
                optionLabels
            )
            b.etDropdown.setAdapter(adapter)

            b.etDropdown.setOnItemClickListener { _, _, index, _ ->
                val selectedText = optionLabels[index]
                val numericValue = q.answers[index].numericValue
                responseItems[position] = responseItems[position].copy(
                    selectedAnswer = selectedText,
                    numericalValue = numericValue
                )
            }

            val prev = responseItems[position]
            if (prev.selectedAnswer.isNotBlank()) {
                b.etDropdown.setText(prev.selectedAnswer, false)
            } else {
                b.etDropdown.setText("", false)
            }
        }
    }
}
