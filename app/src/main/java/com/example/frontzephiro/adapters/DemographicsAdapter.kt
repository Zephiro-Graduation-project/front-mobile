// DemographicsAdapter.kt
package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.frontzephiro.databinding.ItemQuestionCardDemographicsBinding
import com.example.frontzephiro.models.Question
import com.example.frontzephiro.models.ResponseItem

class DemographicsAdapter(
    private var questions: List<Question>,
    private val readOnly: Boolean = false
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

    /** Para Activities: obtener respuestas */
    fun getResponses(): List<ResponseItem> = responseItems

    /** Para recargar preguntas */
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

    /** Prefill en sólo‑lectura */
    fun setResponses(resps: List<ResponseItem>) {
        resps.forEach { resp ->
            val idx = responseItems.indexOfFirst { it.question == resp.question }
            if (idx >= 0) responseItems[idx] = resp
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuestionCardDemographicsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    override fun getItemCount(): Int = questions.size

    inner class ViewHolder(private val b: ItemQuestionCardDemographicsBinding)
        : RecyclerView.ViewHolder(b.root) {

        fun bind(q: Question, position: Int) {
            b.preguntaGeneral.text = q.text
            b.textoGeneral.text = q.measures
                .firstOrNull()
                ?.replace('_', ' ')
                ?.capitalize()
                ?: ""

            val labels = q.answers.map { it.text }
            val arrAdapter = ArrayAdapter(
                b.etDropdown.context,
                android.R.layout.simple_list_item_1,
                labels
            )
            b.etDropdown.setAdapter(arrAdapter)

            if (readOnly) {
                // Sólo‑lectura: fijar texto y deshabilitar
                val resp = responseItems[position]
                b.etDropdown.setText(resp.selectedAnswer, false)
                b.etDropdown.isEnabled = false
            } else {
                // Modo normal: escuchar selección
                b.etDropdown.setOnItemClickListener { _, _, idx, _ ->
                    responseItems[position] = responseItems[position].copy(
                        selectedAnswer = labels[idx],
                        numericalValue = q.answers[idx].numericValue
                    )
                }
                b.etDropdown.setText("", false)
            }
        }
    }
}
