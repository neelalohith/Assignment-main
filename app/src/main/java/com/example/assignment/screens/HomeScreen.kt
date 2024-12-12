package com.example.assignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.assignment.viewmodel.AuthViewModel

@Composable
fun HomeScreen(navController: NavHostController, viewModel: AuthViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Welcome to the Home Screen!")
                Spacer(modifier=Modifier.height(16.dp))

                Button(onClick={
                    viewModel.logout() // Call a logout function in your ViewModel

                    navController.navigate("auth") {
                        popUpTo("home") { inclusive=true } // Clear back stack if needed.
                    }
                }) {
                    Text("Logout")
                }
            }
        }
    )
}
