package com.example.assignment.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.lightColors
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define the light color scheme using the colors defined in Color.kt
private val LightColorPalette = lightColors(
    primary = ButtonColor,
    primaryVariant = Color(0xFF3700B3), // Define a variant color if needed
    secondary = Color(0xFF03DAC5), // Define a secondary color if needed
    background = LightBackground,
    surface = LightBackground,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
)

// Define the dark color scheme (you can customize these colors)
private val DarkColorPalette = darkColors(
    primary = ButtonColor,
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC5),
    background = Color(0xFF121212), // Dark background color
    surface = Color(0xFF1E1E1E), // Dark surface color
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun AssignmentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Determine which color palette to use based on the theme
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography, // Ensure Typography is defined elsewhere in your theme files
        shapes = Shapes, // Ensure Shapes is defined elsewhere in your theme files
        content = content
    )
}