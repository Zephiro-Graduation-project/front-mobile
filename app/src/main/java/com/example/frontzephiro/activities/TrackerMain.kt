package com.example.frontzephiro.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import android.graphics.drawable.ColorDrawable
import android.graphics.Color
import com.example.frontzephiro.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class TrackerMain : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker_main)

        calendarView = findViewById(R.id.calendarView)

        // Set month to August 2025
        val august2025 = CalendarDay.from(2025, 7, 1) // Meses empiezan en 0, así que 7 = Agosto
        calendarView.setCurrentDate(august2025)
        calendarView.setSelectedDate(august2025)

        // Fechas marcadas
        val fechasMarcadas = listOf(
            CalendarDay.from(2025, 7, 1),
            CalendarDay.from(2025, 7, 2),
            CalendarDay.from(2025, 7, 3),
            CalendarDay.from(2025, 7, 4),
            CalendarDay.from(2025, 7, 7),
            CalendarDay.from(2025, 7, 8),
            CalendarDay.from(2025, 7, 9),
            CalendarDay.from(2025, 7, 11),
            CalendarDay.from(2025, 7, 14),
            CalendarDay.from(2025, 7, 15),
            CalendarDay.from(2025, 7, 16),
            CalendarDay.from(2025, 7, 17),
            CalendarDay.from(2025, 7, 18),
            CalendarDay.from(2025, 7, 19),
            CalendarDay.from(2025, 7, 20),
            CalendarDay.from(2025, 7, 21),
            CalendarDay.from(2025, 7, 24),
            CalendarDay.from(2025, 7, 25)
        )

        calendarView.addDecorator(MarkedDatesDecorator(fechasMarcadas))


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menuSeguimiento
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuInicio -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                /*
                R.id.menuSeguimiento -> {
                    startActivity(Intent(this, TrackerMain::class.java))
                    true
                }*/
                R.id.menuJardin -> {
                    startActivity(Intent(this, GardenMain::class.java))
                    true
                }
                R.id.menuContenido -> {
                    startActivity(Intent(this, ContentActivity::class.java))
                    true
                }
                R.id.menuPerfil -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

    }

    // Decorador para fechas marcadas
    class MarkedDatesDecorator(private val dates: Collection<CalendarDay>) : DayViewDecorator {
        private val drawable = ColorDrawable(Color.parseColor("#C2BAF2")) // Color lila

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(drawable)
        }
    }
}
