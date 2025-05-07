package com.example.frontzephiro.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.models.ScoreEntry
import com.example.frontzephiro.network.RetrofitClient
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class GraficaActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var anxietyChart: LineChart
    private lateinit var stressChart: LineChart
    private val api by lazy { RetrofitClient.getAuthenticatedGraphicApi(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grafica)

        val backContainer = findViewById<LinearLayout>(R.id.backContainer)

        backContainer.setOnClickListener {
            onBackPressed()
        }

        progressBar  = findViewById(R.id.progressBar)
        anxietyChart = findViewById(R.id.anxietyChart)
        stressChart  = findViewById(R.id.stressChart)

        findViewById<LottieAnimationView>(R.id.call).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@GraficaActivity, EmergencyContactsActivity::class.java))
            }
        }
        findViewById<LottieAnimationView>(R.id.alert).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@GraficaActivity, EmergencyNumbersActivity::class.java))
            }
        }

        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            selectedItemId = R.id.menuPerfil
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menuInicio    -> startActivity(Intent(this@GraficaActivity, HomeActivity::class.java))
                    R.id.menuContenido -> startActivity(Intent(this@GraficaActivity, ContentActivity::class.java))
                    R.id.menuSeguimiento -> startActivity(Intent(this@GraficaActivity, SeguimientoActivity::class.java))
                    R.id.menuJardin    -> startActivity(Intent(this@GraficaActivity, GardenMain::class.java))
                    R.id.menuPerfil    -> startActivity(Intent(this@GraficaActivity, ProfileActivity::class.java))
                }
                true
            }
        }
        loadAndDraw()
    }

    private fun loadAndDraw() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "") ?: ""
        if (userId.isBlank()) {
            Toast.makeText(this, "Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val scores = api.getScores(userId)
                if (scores.isEmpty()) {
                    Toast.makeText(this@GraficaActivity, "No hay datos para mostrar", Toast.LENGTH_SHORT).show()
                } else {
                    drawCharts(scores)
                }
            } catch (e: Exception) {
                Toast.makeText(this@GraficaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun drawCharts(scores: List<ScoreEntry>) {
        val labels = scores.map { it.date }

        val primaryColor   = ContextCompat.getColor(this, R.color.primary)
        val secondaryColor = ContextCompat.getColor(this, R.color.secondary)

        fun setupChart(chart: LineChart) {
            chart.apply {
                description.isEnabled   = false
                setTouchEnabled(true)
                legend.isEnabled        = false
                axisRight.isEnabled     = false
                axisLeft.axisMinimum    = 0f   // rango mínimo en 0
                xAxis.apply {
                    position            = XAxis.XAxisPosition.BOTTOM
                    granularity         = 1f
                    setDrawGridLines(false)
                    labelRotationAngle  = 90f  // fechas en vertical
                    valueFormatter      = IndexAxisValueFormatter(labels)
                }
            }
        }
        setupChart(anxietyChart)
        setupChart(stressChart)

        val anxietyEntries = scores.mapIndexed { i, s ->
            Entry(i.toFloat(), s.anxietyScore.toFloat())
        }
        val stressEntries  = scores.mapIndexed { i, s ->
            Entry(i.toFloat(), s.stressScore.toFloat())
        }

        anxietyChart.data = LineData(
            LineDataSet(anxietyEntries, "Ansiedad").apply {
                color           = primaryColor
                setCircleColor(primaryColor)
                circleRadius    = 4f
                lineWidth       = 3f
                setDrawValues(false)
            }
        )
        anxietyChart.invalidate()

        stressChart.data = LineData(
            LineDataSet(stressEntries, "Estrés").apply {
                color           = secondaryColor
                setCircleColor(secondaryColor)
                circleRadius    = 4f
                lineWidth       = 3f
                setDrawValues(false)
            }
        )
        stressChart.invalidate()
    }
}
