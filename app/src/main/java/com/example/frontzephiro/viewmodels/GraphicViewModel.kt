// app/src/main/java/com/example/frontzephiro/viewmodels/GraphicViewModel.kt
package com.example.frontzephiro.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import com.example.frontzephiro.models.ScoreEntry
import com.example.frontzephiro.network.RetrofitClient
import kotlinx.coroutines.launch

class GraphicViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val userId = prefs.getString("USER_ID", "") ?: ""
    private val api = RetrofitClient.getAuthenticatedGraphicApi(app)

    var scores by mutableStateOf<List<ScoreEntry>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        if (userId.isNotBlank()) fetchScores() else isLoading = false
    }

    private fun fetchScores() {
        viewModelScope.launch {
            isLoading = true
            scores = try {
                api.getScores(userId)
            } catch (e: Exception) {
                emptyList()
            }
            isLoading = false
        }
    }
}
