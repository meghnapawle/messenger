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

                // This effect handles automatic navigation for the server device
                // when a client connects to it.
                LaunchedEffect(key1 = bluetoothManager) {
                    bluetoothManager.connectionState.collect { state ->
                        if (state is ConnectionState.Connected && currentScreen !is Screen.Chat) {
                            currentScreen = Screen.Chat(state.deviceName ?: "Unknown", state.deviceAddress)
                        }
                    }
                }

                when (val screen = currentScreen) {
                    is Screen.Splash -> {
                        SplashScreen {
                            currentScreen = Screen.Onboarding
                        }
                    }
                    is Screen.Onboarding -> {
                        OnboardingScreen {
                            currentScreen = Screen.Permissions
                        }
                    }
                    is Screen.Permissions -> {
                        BluetoothPermissionScreen(
                            activity = activity,
                            onAllReady = {
                                currentScreen = Screen.DeviceList
                            }
                        )
                    }
                    is Screen.DeviceList -> {
                        DeviceDiscoveryScreen(
                            bluetoothManager = bluetoothManager,
                            onDeviceSelected = { deviceName, deviceAddress ->
                                // This handles the client-side navigation.
                                currentScreen = Screen.Chat(deviceName, deviceAddress)
                            },
                            onBack = {
                                currentScreen = Screen.Permissions
                            }
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
                                currentScreen = Screen.DeviceList
                            }
                        )
                    }
                }
            }
        }
    }
}
