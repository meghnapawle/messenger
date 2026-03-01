package com.example.bluemesh.ui

/**
 * Represents the different screens in the app navigation flow
 */
sealed class Screen {
    data object Splash : Screen()
    data object Onboarding : Screen()
    data object Permissions : Screen()
    data object DeviceList : Screen()
    data class Chat(val deviceName: String, val deviceAddress: String) : Screen()
}
