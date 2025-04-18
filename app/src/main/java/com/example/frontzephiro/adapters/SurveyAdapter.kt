package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.frontzephiro.R
import com.example.frontzephiro.databinding.ItemQuestionCardBinding
import com.example.frontzephiro.models.Question
import com.google.android.material.slider.LabelFormatter

class SurveyAdapter(
    private val questions: List<Question>
) : RecyclerView.Adapter<SurveyAdapter.ViewHolder>() {

    inner class ViewHolder(private val b: ItemQuestionCardBinding)
        : RecyclerView.ViewHolder(b.root) {

        fun bind(q: Question) {
            // 1) Título dinámico
            b.preguntaGeneral.text = q.text

            // 2) Subtítulo según el primer measure
            val measure = q.measures.firstOrNull()
            b.textoGeneral.text = when (measure) {
                "Stress"           -> b.textoGeneral.context.getString(R.string.nivelEstres)
                "Anxiety"          -> b.textoGeneral.context.getString(R.string.nivelAnsiedad)
                "Sleep"            -> b.textoGeneral.context.getString(R.string.nivelSueno)
                "Control"          -> b.textoGeneral.context.getString(R.string.nivelControl)
                "Calm"             -> b.textoGeneral.context.getString(R.string.nivelCalma)
                "Thoughts"         -> b.textoGeneral.context.getString(R.string.nivelPensamientos)
                "Physical Activity"-> b.textoGeneral.context.getString(R.string.nivelActividadFisica)
                "Nutrition"        -> b.textoGeneral.context.getString(R.string.nivelAlimentacion)
                else               -> ""
            }

            // 3) Configurar Slider con rango y etiquetas
            val min = q.answers.minOf { it.numericValue }.toFloat()
            val max = q.answers.maxOf { it.numericValue }.toFloat()
            b.rangeSliderGeneral.apply {
                valueFrom = min
                valueTo   = max
                stepSize  = 1f
                value     = min
                setLabelFormatter(LabelFormatter { value ->
                    q.answers.firstOrNull { it.numericValue == value.toInt() }
                        ?.text
                        ?: value.toInt().toString()
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuestionCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(questions[position])
    }

    override fun getItemCount(): Int = questions.size
}
