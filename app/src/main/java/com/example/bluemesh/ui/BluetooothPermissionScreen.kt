// CURSOR TEST
package com.example.bluemesh.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun BluetoothPermissionScreen(
    activity: Activity,
    onAllReady: () -> Unit
) {
    val context = LocalContext.current
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    
    var bluetoothEnabled by remember { mutableStateOf(bluetoothAdapter?.isEnabled == true) }
    var permissionGranted by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }

    // Check if device has Bluetooth hardware
    val hasBluetooth = remember { bluetoothAdapter != null }

    // Determine which permissions are needed based on Android version
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+)
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            // Android 8.0 - 11 (API 26-30)
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Check current permission status
    val hasPermissions = remember {
        permissionsToRequest.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun checkBluetoothAndProceed() {
        if (hasPermissions && bluetoothEnabled) {
            onAllReady()
        }
    }

    // Launcher for permission requests
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        permissionGranted = allGranted
        
        if (allGranted) {
            showError = null
            // Check if Bluetooth is enabled after permissions granted
            checkBluetoothAndProceed()
        } else {
            showError = "Permissions are required for Bluetooth discovery and connection"
        }
    }

    // Launcher for enabling Bluetooth
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        bluetoothEnabled = bluetoothAdapter?.isEnabled == true
        if (bluetoothEnabled) {
            showError = null
            checkBluetoothAndProceed()
        } else {
            showError = "Bluetooth must be enabled to use BlueMesh"
        }
    }

    // Check permissions on first load
    LaunchedEffect(Unit) {
        permissionGranted = hasPermissions
        if (hasPermissions && bluetoothEnabled) {
            onAllReady()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!hasBluetooth) {
            Text(
                text = "Bluetooth Not Available",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This device does not have Bluetooth hardware",
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Bluetooth Permissions",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "BlueMesh needs Bluetooth permissions to discover nearby devices and connect for messaging.",
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Show error if any
            showError?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Request permissions button
            if (!hasPermissions) {
                Button(
                    onClick = {
                        permissionLauncher.launch(permissionsToRequest)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permissions")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Enable Bluetooth button
            if (!bluetoothEnabled) {
                Button(
                    onClick = {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        enableBluetoothLauncher.launch(enableBtIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasPermissions
                ) {
                    Text("Enable Bluetooth")
                }
            }

            // Status indicators
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Permissions:")
                Text(
                    text = if (hasPermissions) "✓ Granted" else "✗ Not Granted",
                    color = if (hasPermissions) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bluetooth:")
                Text(
                    text = if (bluetoothEnabled) "✓ Enabled" else "✗ Disabled",
                    color = if (bluetoothEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
