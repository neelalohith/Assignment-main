package com.example.assignment.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // State to track navigation readiness
    val isReadyForNavigation = mutableStateOf(false)

    init {
        startSplashTimer()
    }

    /**
     * Function to start the splash delay timer.
     * Waits for 3 seconds, then sets the navigation state to true.
     */
    private fun startSplashTimer() {
        viewModelScope.launch {
            delay(2000) // Wait for 2 seconds
            isReadyForNavigation.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel any active coroutine when ViewModel is destroyed
        viewModelScope.cancel()
    }
}