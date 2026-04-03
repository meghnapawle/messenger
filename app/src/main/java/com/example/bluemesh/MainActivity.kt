package com.example.bluemesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.bluemesh.bluetooth.BluetoothChatManager
import com.example.bluemesh.bluetooth.ConnectionState
import com.example.bluemesh.ui.*
import com.example.bluemesh.ui.theme.BlueMeshTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BlueMeshTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
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

                when (val screen = currentScreen) {
                    is Screen.Splash -> {
                        SplashScreen { currentScreen = Screen.Onboarding }
                    }
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
                    is Screen.Groups -> {
                        GroupsScreen(onTabSelected = { currentScreen = it })
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
                    else -> {}
                }
            }
        }
    }
}
