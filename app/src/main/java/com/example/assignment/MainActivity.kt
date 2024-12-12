package com.example.assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.assignment.screens.AuthScreen
import com.example.assignment.screens.HomeScreen
import com.example.assignment.screens.OnboardingScreen
import com.example.assignment.screens.SplashScreen
import com.example.assignment.viewmodel.AuthViewModel
import com.example.assignment.viewmodel.AuthViewModelFactory
import com.example.assignment.viewmodel.OnboardingViewModel
import com.example.assignment.viewmodel.SplashViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    MainScreen(navController = navController)
                }
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    // Create an instance of AuthViewModel using the factory.
    val authViewModelFactory = AuthViewModelFactory(LocalContext.current)
    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)

    // Create other ViewModels as needed.
    val splashViewModel: SplashViewModel = viewModel()
    val onboardingViewModel: OnboardingViewModel = viewModel()

    // Set up Navigation
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                viewModel = splashViewModel,
                navController = navController
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                viewModel = onboardingViewModel,
                navController = navController
            )
        }
        composable("auth") {
            AuthScreen(
                viewModel = authViewModel,
                navController = navController
            )
        }
        composable("home") {
            HomeScreen(navController = navController, viewModel = authViewModel)
        }
    }
}