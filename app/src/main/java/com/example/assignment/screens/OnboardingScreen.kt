package com.example.assignment.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.assignment.viewmodel.OnboardingViewModel
import kotlinx.coroutines.delay
import com.google.accompanist.pager.*
import com.example.assignment.ui.theme.* // Import your defined colors

// Data class for onboarding slides.
data class OnboardingItem(
    val title: String,
    val description: String,
    val color: Color // This will now be the same for all pages.
)

// Create onboarding content with consistent light colors.
val onboardingPages = listOf(
    OnboardingItem("Welcome", "Discover new features with us", OnboardingPageColor),
    OnboardingItem("Explore", "Navigate across content seamlessly", OnboardingPageColor),
    OnboardingItem("Get Started", "Sign in & personalize your experience", OnboardingPageColor)
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(navController: NavHostController, viewModel: OnboardingViewModel) {
    val pagerState = rememberPagerState()

    // Timer to automatically change pages every 3 seconds.
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Wait for 3 seconds.
            val nextPage = (pagerState.currentPage + 1) % onboardingPages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    // HorizontalPager to swipe through onboarding pages.
    HorizontalPager(
        count = onboardingPages.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        OnboardingPage(onboardingPages[page])
    }

    // Add "Skip" Button at the bottom of the screen.
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.BottomEnd // Align to the bottom end (right).
    ) {
        Button(
            onClick = {
                navController.navigate("auth") {
                    popUpTo("splash") { inclusive = true }
                }
            },
            modifier = Modifier.padding(bottom = 16.dp) // Add padding to avoid overlap with bottom navigation or status bar.
        ) {
            Text("Authenticate")
        }
    }
}

@Composable
fun OnboardingPage(page: OnboardingItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().background(page.color).padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = page.title,
            style = MaterialTheme.typography.h4.copy(color = PrimaryText), // Use primary text color for contrast against light background.
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.body1.copy(color = SecondaryText), // Use secondary text color for description text.
        )
    }
}