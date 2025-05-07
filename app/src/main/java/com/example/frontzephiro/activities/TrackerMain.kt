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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker_main)

        // Animaciones y redirecciones
        findViewById<LottieAnimationView>(R.id.call).apply {
            repeatCount = 0
            playAnimation()
            setOnClickListener {
                startActivity(Intent(this@TrackerMain, EmergencyContactsActivity::class.java))
            }
        }
        findViewById<LottieAnimationView>(R.id.alert).apply {
            repeatCount = 0
            playAnimation()
            setOnClickListener {
                startActivity(Intent(this@TrackerMain, EmergencyNumbersActivity::class.java))
            }
        }

        // Inicializar calendario
        calendarView = findViewById(R.id.calendarView)
        calendarView.setCurrentDate(CalendarDay.today())
        calendarView.setSelectedDate(CalendarDay.today())

        // Recuperar userId de SharedPreferences
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "") ?: ""
        if (userId.isBlank()) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
        } else {
            // Llamada para obtener fechas marcadas
            questionnaireService = RetrofitClient
                .getAuthenticatedArtifactClient(this)
                .create(QuestionnaireApiService::class.java)

            questionnaireService.getQuestionnaireDates(userId)
                .enqueue(object : Callback<List<String>> {
                    override fun onResponse(call: Call<List<String>>, resp: Response<List<String>>) {
                        if (!resp.isSuccessful || resp.body().isNullOrEmpty()) {
                            Toast.makeText(
                                this@TrackerMain,
                                "Error cargando fechas: ${resp.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        // Limpiar decoradores anteriores
                        calendarView.removeDecorators()

                        // Convertir "yyyy-MM-dd" a CalendarDay (mes - 1)
                        val markedDates = resp.body()!!
                            .mapNotNull { iso ->
                                iso.split("-").takeIf { it.size == 3 }?.let { (y, m, d) ->
                                    val year  = y.toIntOrNull() ?: return@let null
                                    val month = (m.toIntOrNull() ?: return@let null) - 1
                                    val day   = d.toIntOrNull() ?: return@let null
                                    CalendarDay.from(year, month, day)
                                }
                            }

                        // Pintar fechas
                        calendarView.addDecorator(MarkedDatesDecorator(this@TrackerMain, markedDates))
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

        // Cuando el usuario selecciona un día, navegar a ShowQuestionariesActivity
        calendarView.setOnDateChangedListener { _, date, selected ->
            if (!selected) return@setOnDateChangedListener

            // Formatear yyyy-MM-dd
            val year  = date.year
            val month = date.month + 1  // CalendarDay.month is 0-based
            val day   = date.day
            val dateStr = String.format("%04d-%02d-%02d", year, month, day)

            // Recuperar userId de nuevo
            val uid = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("USER_ID", "") ?: ""
            if (uid.isBlank()) {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
                return@setOnDateChangedListener
            }

            // Lanzar la nueva pantalla
            startActivity(
                Intent(this, ShowQuestionnariesActivity::class.java).apply {
                    putExtra("USER_ID", uid)
                    putExtra("SELECTED_DATE", dateStr)
                }
            )
        }

        // Bottom Navigation (redirecciones)
        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            selectedItemId = R.id.menuSeguimiento
            setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
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

    // Decorador para fechas marcadas
    class MarkedDatesDecorator(
        context: Context,
        private val dates: Collection<CalendarDay>
    ) : DayViewDecorator {
        private val drawable: Drawable =
            ContextCompat.getDrawable(context, R.drawable.circle_day_background)!!

        override fun shouldDecorate(day: CalendarDay): Boolean =
            dates.contains(day)

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(drawable)
        }
    }
}
