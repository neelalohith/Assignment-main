package com.example.assignment.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AuthViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(context) as T // Create instance of AuthViewModel with context only
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
