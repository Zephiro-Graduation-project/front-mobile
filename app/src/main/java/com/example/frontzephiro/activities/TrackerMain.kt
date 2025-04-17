package com.example.frontzephiro.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import android.graphics.drawable.ColorDrawable
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.frontzephiro.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class TrackerMain : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker_main)

        calendarView = findViewById(R.id.calendarView)


        val today = CalendarDay.today()
        calendarView.setCurrentDate(today)
        calendarView.setSelectedDate(today)

        // Fechas marcadas
        val fechasMarcadas = listOf(
            CalendarDay.from(2025, 2, 30),
            CalendarDay.from(2025, 3, 1),
            CalendarDay.from(2025, 3, 2),
            CalendarDay.from(2025, 3, 3),
            CalendarDay.from(2025, 3, 4),
            CalendarDay.from(2025, 3, 7),
            CalendarDay.from(2025, 3, 8),
            CalendarDay.from(2025, 3, 9),
            CalendarDay.from(2025, 3, 11),
            CalendarDay.from(2025, 3, 12),
            CalendarDay.from(2025, 3, 13),
            CalendarDay.from(2025, 3, 14),
            CalendarDay.from(2025, 3, 15),
            CalendarDay.from(2025, 3, 16)
        )

        calendarView.addDecorator(MarkedDatesDecorator(this, fechasMarcadas))



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
    class MarkedDatesDecorator(
        private val context: Context,
        private val dates: Collection<CalendarDay>
    ) : DayViewDecorator {

        private val drawable: Drawable = ContextCompat.getDrawable(context, R.drawable.circle_day_background)!!

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(drawable)
        }
    }

}
