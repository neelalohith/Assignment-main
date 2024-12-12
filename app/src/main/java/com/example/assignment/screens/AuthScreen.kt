package com.example.assignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.assignment.viewmodel.AuthResult
import com.example.assignment.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(navController: NavHostController, viewModel: AuthViewModel) {
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Coroutine scope for launching suspend functions
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display error message if any
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colors.error)
        }

        // Sign in with Google button
        Button(
            onClick = {
                // Sign in with Google when button is clicked
                coroutineScope.launch {
                    viewModel.signInWithGoogle(navController).let { result ->
                        if (result is AuthResult.Error) {
                            errorMessage = result.message // Show error message
                        }
                    }
                }
            },
            modifier = Modifier.padding(bottom = 16.dp) // Add some space below the button
        ) {
            Text("Sign in with Google")
        }

        // Phone Number Input Field
        if (!isOtpSent) {
            TextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    // Send OTP when button is clicked
                    coroutineScope.launch {
                        viewModel.sendPhoneOTP(phoneNumber).let { result ->
                            if (result is AuthResult.Success) {
                                isOtpSent = true // OTP sent successfully
                            } else if (result is AuthResult.Error) {
                                errorMessage = result.message // Show error message
                            }
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Send OTP")
            }
        } else {
            // OTP Input Field
            TextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text("Enter OTP") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    // Verify OTP when button is clicked, using coroutine scope to call the suspend function
                    coroutineScope.launch {
                        viewModel.verifyOTP(otp).let { result ->
                            if (result is AuthResult.Success) {
                                navController.navigate("home") // Navigate to home screen on success
                            } else if (result is AuthResult.Error) {
                                errorMessage = result.message // Show error message
                            }
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Verify OTP")
            }
        }
    }
}