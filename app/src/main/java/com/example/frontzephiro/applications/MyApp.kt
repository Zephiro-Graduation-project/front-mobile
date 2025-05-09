package com.example.frontzephiro.applications

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.example.frontzephiro.activities.DemographicsActivity
import com.example.frontzephiro.activities.DiurnaActivity
import com.example.frontzephiro.activities.GadActivity
import com.example.frontzephiro.activities.HabitsActivity
import com.example.frontzephiro.activities.LoginActivity
import com.example.frontzephiro.activities.NocturnaActivity
import com.example.frontzephiro.activities.PssActivity
import com.example.frontzephiro.activities.RegisterActivity
import java.text.SimpleDateFormat
import java.util.*

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                // 1) No interferir si ya estamos dentro de cualquiera de las dos
                if (activity is
                            DiurnaActivity ||
                    activity is
                            NocturnaActivity ||
                    activity is
                            RegisterActivity ||
                    activity is
                            LoginActivity ||
                    activity is
                            HabitsActivity ||
                    activity is
                            PssActivity ||
                    activity is
                            GadActivity ||
                    activity is
                            DemographicsActivity
                        ) return

                val now = Calendar.getInstance()
                val hour = now.get(Calendar.HOUR_OF_DAY)
                val prefs = activity.getSharedPreferences("AppPrefs", MODE_PRIVATE)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date())

                // ——— Matutino: 06–11h ——
                if (hour in 6..11) {
                    val diurnaDone = prefs.getString("DIURNO_SURVEY_DATE", "") == today
                    if (!diurnaDone) {
                        val i = Intent(activity,
                            DiurnaActivity::class.java)
                            .apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra("READ_ONLY", false)
                            }
                        activity.startActivity(i)
                        return
                    }
                }

                // ——— Nocturno: 17–23h ——
                if (hour in 18..23) {
                    val nocturnaDone = prefs.getString("NOCTURNO_SURVEY_DATE", "") == today
                    if (!nocturnaDone) {
                        val i = Intent(activity,
                            NocturnaActivity::class.java)
                            .apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra("READ_ONLY", false)
                            }
                        activity.startActivity(i)
                    }
                }
            }

            // resto de callbacks vacíos:
            override fun onActivityCreated(a: Activity, b: Bundle?) {}
            override fun onActivityStarted(a: Activity) {}
            override fun onActivityPaused(a: Activity) {}
            override fun onActivityStopped(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
            override fun onActivityDestroyed(a: Activity) {}
        })
    }
}
