package com.example.bluemesh.ui

sealed class Screen {
    data object Onboarding : Screen()
    data object ProfileSetup : Screen()
    data object Permissions : Screen()
    
    // Main App Screens (Tabs)
    data object Mesh : Screen()
    data object Groups : Screen()
    data object Friends : Screen()
    data object Profile : Screen()

    data class Chat(val deviceName: String, val deviceAddress: String) : Screen()
}
