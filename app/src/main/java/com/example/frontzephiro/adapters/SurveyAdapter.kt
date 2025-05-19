package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.frontzephiro.R
import com.example.frontzephiro.databinding.ItemQuestionCardBinding
import com.example.frontzephiro.models.Question
import com.example.frontzephiro.models.ResponseItem
import com.google.android.material.slider.LabelFormatter

class SurveyAdapter(
    private var questions: List<Question>,
    private val readOnly: Boolean = false
) : RecyclerView.Adapter<SurveyAdapter.ViewHolder>() {

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

    /** Para inyectar respuestas en modo sólo-lectura */
    fun setResponses(resps: List<ResponseItem>) {
        resps.forEach { resp ->
            val idx = responseItems.indexOfFirst { it.question == resp.question }
            if (idx >= 0) {
                responseItems[idx] = resp
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuestionCardBinding.inflate(
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

    inner class ViewHolder(private val b: ItemQuestionCardBinding)
        : RecyclerView.ViewHolder(b.root) {

        fun bind(q: Question, position: Int) {
            b.preguntaGeneral.text = q.text

            val measure = q.measures.firstOrNull()
            b.textoGeneral.text = when (measure) {
                "Stress"            -> b.textoGeneral.context.getString(R.string.nivelEstres)
                "Anxiety"           -> b.textoGeneral.context.getString(R.string.nivelAnsiedad)
                "Sleep"             -> b.textoGeneral.context.getString(R.string.nivelSueno)
                "Control"           -> b.textoGeneral.context.getString(R.string.nivelControl)
                "Calm"              -> b.textoGeneral.context.getString(R.string.nivelCalma)
                "Thoughts"          -> b.textoGeneral.context.getString(R.string.nivelPensamientos)
                "Physical Activity" -> b.textoGeneral.context.getString(R.string.nivelActividadFisica)
                "Nutrition"         -> b.textoGeneral.context.getString(R.string.nivelAlimentacion)
                else                -> ""
            }

            val min = q.answers.minOf { it.numericValue }.toFloat()
            val max = q.answers.maxOf { it.numericValue }.toFloat()

            b.rangeSliderGeneral.apply {
                clearOnChangeListeners()

                valueFrom = min
                valueTo   = max
                stepSize  = 1f

                val saved = responseItems[position].numericalValue
                value = saved.coerceIn(min.toInt(), max.toInt()).toFloat()

                setLabelFormatter(LabelFormatter { v ->
                    q.answers.firstOrNull { it.numericValue == v.toInt() }?.text
                        ?: v.toInt().toString()
                })

                isEnabled = !readOnly

                if (!readOnly) {
                    addOnChangeListener { _, v, _ ->
                        val iv = v.toInt()
                        val tv = q.answers.first { it.numericValue == iv }.text
                        responseItems[position] = responseItems[position].copy(
                            selectedAnswer = tv,
                            numericalValue = iv
                        )
                    }
                }
            }
        }
    }
}
