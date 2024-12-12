package com.example.assignment.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Import Color for light colors
import androidx.compose.ui.unit.dp
import com.example.assignment.viewmodel.SplashViewModel

@Composable
fun SplashScreen(navController: NavHostController, viewModel: SplashViewModel) {
    // Navigate to onboarding after a delay
    LaunchedEffect(Unit) {
        delay(3000) // Wait for 3 seconds before navigating to onboarding screen.
        navController.navigate("onboarding") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // Render a light background color for the splash screen with welcome text.
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFFE0F7FA)), // Light cyan color (you can change this to any light color)
        contentAlignment = Alignment.Center // Center the content in the box.
    ) {
        Text(
            text = "Authenticator app", // Your welcome message.
            color = Color.Black, // Set text color to black for contrast.
            modifier = Modifier.padding(16.dp), // Add padding around the text.
            style = androidx.compose.material.MaterialTheme.typography.h4 // Use Material typography for styling.
        )
    }
}