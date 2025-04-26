// GraphicScreen.kt
package com.example.frontzephiro.activities

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontzephiro.viewmodels.GraphicViewModel
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.piechart.animation.simpleChartAnimation
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.line.renderer.yaxis.SimpleYAxisDrawer

class GraficaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GraphicScreen()
        }
    }
}

@Composable
fun GraphicScreen(
    viewModel: GraphicViewModel = viewModel(factory =
    androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
        LocalContext.current.applicationContext as Application
    )
    )
) {
    if (viewModel.isLoading) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (viewModel.scores.isEmpty()) {
        Text(
            "No hay datos para mostrar",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    // 1) mapeo
    val scores = viewModel.scores
    val labels = scores.map { it.date }
    val anxietyData = listOf(
        LineChartData(
            points = scores.map { LineChartData.Point(it.anxietyScore.toFloat(), it.date) },
            lineDrawer = SolidLineDrawer()
        )
    )
    val stressData = listOf(
        LineChartData(
            points = scores.map { LineChartData.Point(it.stressScore.toFloat(), it.date) },
            lineDrawer = SolidLineDrawer()
        )
    )

    // 2) UI
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Ansiedad", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LineChart(
            linesChartData = anxietyData,
            labels = labels,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            animation = simpleChartAnimation(),
            pointDrawer = FilledCircularPointDrawer(),
            xAxisDrawer = SimpleXAxisDrawer(),
            yAxisDrawer = SimpleYAxisDrawer(),
            horizontalOffset = 5f
        )

        Spacer(Modifier.height(24.dp))

        Text("Estrés", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LineChart(
            linesChartData = stressData,
            labels = labels,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            animation = simpleChartAnimation(),
            pointDrawer = FilledCircularPointDrawer(),
            xAxisDrawer = SimpleXAxisDrawer(),
            yAxisDrawer = SimpleYAxisDrawer(),
            horizontalOffset = 5f
        )
    }
}
