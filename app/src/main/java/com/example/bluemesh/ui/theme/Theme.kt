package com.example.bluemesh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BlueColorScheme = lightColorScheme()

@Composable
fun BlueMeshTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlueColorScheme,
        content = content
    )
}



