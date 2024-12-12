package com.example.assignment.viewmodel

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import androidx.lifecycle.ViewModel
import com.example.assignment.screens.OnboardingItem
import com.example.assignment.ui.theme.OnboardingPageColor // Import the defined onboarding color

class OnboardingViewModel : ViewModel() {

    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Manage the current onboarding page index
    var currentPageIndex = mutableStateOf(0)

    // List of onboarding pages using the same color for all
    private val onboardingPages = listOf(
        OnboardingItem("Welcome", "Discover new features with us", OnboardingPageColor),
        OnboardingItem("Explore", "Navigate across content seamlessly", OnboardingPageColor),
        OnboardingItem("Get Started", "Sign in & personalize your experience", OnboardingPageColor)
    )

    init {
        autoSlidePages()
    }

    /**
     * Coroutine to handle the automated onboarding slide change
     */
    private fun autoSlidePages() {
        viewModelScope.launch {
            while (true) {
                delay(2000) // Wait for 2 seconds
                currentPageIndex.value = (currentPageIndex.value + 1) % onboardingPages.size
            }
        }
    }

    /**
     * Exposes onboarding content dynamically.
     */
    fun getCurrentOnboardingPage(): OnboardingItem {
        return onboardingPages[currentPageIndex.value]
    }
}
