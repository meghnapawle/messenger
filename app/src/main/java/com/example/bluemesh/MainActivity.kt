package com.example.bluemesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.bluemesh.bluetooth.BluetoothChatManager
import com.example.bluemesh.bluetooth.ConnectionState
import com.example.bluemesh.ui.*
import com.example.bluemesh.ui.theme.BlueMeshTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            BlueMeshTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Onboarding) }
                val activity = this@MainActivity
                val bluetoothManager = remember { BluetoothChatManager(applicationContext) }

                DisposableEffect(key1 = bluetoothManager) {
                    onDispose {
                        bluetoothManager.release()
                    }
                }

                LaunchedEffect(key1 = bluetoothManager) {
                    bluetoothManager.connectionState.collect { state ->
                        if (state is ConnectionState.Connected && currentScreen !is Screen.Chat) {
                            currentScreen = Screen.Chat(state.deviceName ?: "Unknown", state.deviceAddress)
                        }
                    }
                }

                // Handle system back button for different screens
                BackHandler(enabled = currentScreen !is Screen.Mesh) {
                    when (currentScreen) {
                        is Screen.Chat -> {
                            bluetoothManager.disconnect()
                            bluetoothManager.clearMessages()
                            currentScreen = Screen.Mesh
                        }
                        is Screen.Global, is Screen.Friends, is Screen.Profile -> {
                            currentScreen = Screen.Mesh
                        }
                        is Screen.ProfileSetup -> {
                            currentScreen = Screen.Onboarding
                        }
                        is Screen.Permissions -> {
                            currentScreen = Screen.ProfileSetup
                        }
                        is Screen.Onboarding -> {
                            activity.finish()
                        }
                        else -> {
                            currentScreen = Screen.Mesh
                        }
                    }
                }

                when (val screen = currentScreen) {
                    is Screen.Onboarding -> {
                        OnboardingScreen { currentScreen = Screen.ProfileSetup }
                    }
                    is Screen.ProfileSetup -> {
                        ProfileSetupScreen { name, vibe ->
                            bluetoothManager.updateProfile(name, vibe)
                            currentScreen = Screen.Permissions
                        }
                    }
                    is Screen.Permissions -> {
                        BluetoothPermissionScreen(
                            activity = activity,
                            onAllReady = { currentScreen = Screen.Mesh }
                        )
                    }
                    is Screen.Mesh -> {
                        DeviceDiscoveryScreen(
                            bluetoothManager = bluetoothManager,
                            onDeviceSelected = { deviceName, deviceAddress ->
                                currentScreen = Screen.Chat(deviceName, deviceAddress)
                            },
                            onEditProfile = { currentScreen = Screen.Profile },
                            onTabSelected = { currentScreen = it }
                        )
                    }
                    is Screen.Global -> {
                        GlobalScreen(
                            bluetoothManager = bluetoothManager,
                            onTabSelected = { currentScreen = it }
                        )
                    }
                    is Screen.Friends -> {
                        FriendsScreen(onTabSelected = { currentScreen = it })
                    }
                    is Screen.Profile -> {
                        ProfileScreen(
                            bluetoothManager = bluetoothManager,
                            onEditProfile = { currentScreen = Screen.ProfileSetup },
                            onTabSelected = { currentScreen = it }
                        )
                    }
                    is Screen.Chat -> {
                        ChatScreen(
                            bluetoothManager = bluetoothManager,
                            deviceName = screen.deviceName,
                            deviceAddress = screen.deviceAddress,
                            onBack = {
                                bluetoothManager.disconnect()
                                bluetoothManager.clearMessages()
                                currentScreen = Screen.Mesh
                            }
                        )
                    }
                }
            }
        }
    }
}
