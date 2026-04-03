package com.example.bluemesh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Based on the image provided: dark forest green background and mint/teal accents
private val DarkGreen = Color(0xFF002B24)
private val MintGreen = Color(0xFF2DE0AD)
private val SurfaceVariant = Color(0xFF003D33)
private val OnDarkGreen = Color.White

private val BlueMeshColorScheme = darkColorScheme(
    primary = MintGreen,
    onPrimary = Color.Black,
    background = DarkGreen,
    onBackground = OnDarkGreen,
    surface = DarkGreen,
    onSurface = OnDarkGreen,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = MintGreen
)

@Composable
fun BlueMeshTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlueMeshColorScheme,
        content = content
    )
}
