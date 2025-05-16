package com.example.frontzephiro.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.api.QuestionnaireApiService
import com.example.frontzephiro.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import androidx.core.content.ContextCompat
import android.graphics.drawable.Drawable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrackerMain : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var questionnaireService: QuestionnaireApiService
    private var markedDates: List<CalendarDay> = emptyList()
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker_main)

        // Vistas de animaciones
        findViewById<LottieAnimationView>(R.id.call).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@TrackerMain, EmergencyContactsActivity::class.java))
            }
        }
        findViewById<LottieAnimationView>(R.id.alert).apply {
            repeatCount = 0; playAnimation()
            setOnClickListener {
                startActivity(Intent(this@TrackerMain, EmergencyNumbersActivity::class.java))
            }
        }

        // Inicializar calendario
        calendarView = findViewById(R.id.calendarView)
        calendarView.setCurrentDate(CalendarDay.today())
        calendarView.setSelectedDate(CalendarDay.today())

        // Obtener userId
        userId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            .getString("USER_ID", "") ?: ""
        if (userId.isBlank()) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
        } else {
            questionnaireService = RetrofitClient
                .getAuthenticatedArtifactClient(this)
                .create(QuestionnaireApiService::class.java)

            // Traer fechas marcadas
            questionnaireService.getQuestionnaireDates(userId)
                .enqueue(object : Callback<List<String>> {
                    override fun onResponse(call: Call<List<String>>, resp: Response<List<String>>) {
                        if (!resp.isSuccessful) {
                            Toast.makeText(
                                this@TrackerMain,
                                "Error cargando fechas: ${resp.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                        val isoDates = resp.body().orEmpty()

                        // Convertir o avisar si no hay ninguno
                        markedDates = isoDates.mapNotNull { iso ->
                            iso.split("-").takeIf { it.size == 3 }?.let { (y, m, d) ->
                                val yy = y.toIntOrNull() ?: return@let null
                                val mm = (m.toIntOrNull() ?: return@let null) - 1
                                val dd = d.toIntOrNull() ?: return@let null
                                CalendarDay.from(yy, mm, dd)
                            }
                        }

                        if (markedDates.isEmpty()) {
                            Toast.makeText(
                                this@TrackerMain,
                                "Aún no has llenado ningún cuestionario",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            calendarView.removeDecorators()
                            calendarView.addDecorator(MarkedDatesDecorator(this@TrackerMain, markedDates))
                        }
                    }
                    override fun onFailure(call: Call<List<String>>, t: Throwable) {
                        Toast.makeText(
                            this@TrackerMain,
                            "Fallo de red: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }

        // Al seleccionar un día
        calendarView.setOnDateChangedListener { widget, date, selected ->
            // Sólo actúa cuando el usuario está intentando seleccionarlo
            if (!selected) return@setOnDateChangedListener

            if (!markedDates.contains(date)) {
                // Quitar la selección que acaba de hacer el usuario
                widget.setDateSelected(date, false)

                // Mostrar sólo el toast
                Toast.makeText(
                    this@TrackerMain,
                    "No hay cuestionarios llenados para este día",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Si estaba marcado, procedemos con la navegación
                val dateStr = String.format(
                    "%04d-%02d-%02d",
                    date.year, date.month + 1, date.day
                )
                startActivity(
                    Intent(this, ShowQuestionnariesActivity::class.java).apply {
                        putExtra("USER_ID", userId)
                        putExtra("SELECTED_DATE", dateStr)
                    }
                )
            }
        }

        // Bottom navigation
        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            selectedItemId = R.id.menuSeguimiento
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menuInicio    -> startActivity(Intent(this@TrackerMain, HomeActivity::class.java))
                    R.id.menuJardin    -> startActivity(Intent(this@TrackerMain, GardenMain::class.java))
                    R.id.menuContenido -> startActivity(Intent(this@TrackerMain, ContentActivity::class.java))
                    R.id.menuPerfil    -> startActivity(Intent(this@TrackerMain, ProfileActivity::class.java))
                    else               -> return@setOnItemSelectedListener false
                }
                true
            }
        }
    }

    // Decorador para pintar círculo en fechas marcadas
    class MarkedDatesDecorator(
        context: Context,
        private val dates: Collection<CalendarDay>
    ) : DayViewDecorator {
        private val drawable: Drawable =
            ContextCompat.getDrawable(context, R.drawable.circle_day_background)!!
        override fun shouldDecorate(day: CalendarDay) = dates.contains(day)
        override fun decorate(view: DayViewFacade) = view.setBackgroundDrawable(drawable)
    }
}
