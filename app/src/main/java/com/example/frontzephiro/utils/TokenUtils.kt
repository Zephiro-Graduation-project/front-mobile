package com.example.frontzephiro.utils

import android.content.Context

object TokenUtils {
    private const val PREFS_NAME = "AppPrefs"
    private const val KEY_TOKEN = "TOKEN"

    @JvmStatic
    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_TOKEN, null)
    }
}
