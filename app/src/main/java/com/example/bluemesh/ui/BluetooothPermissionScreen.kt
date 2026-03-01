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
    var showError by remember { mutableStateOf<String?>(null) }

    val hasBluetooth = remember { bluetoothAdapter != null }

    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION // Added for better compatibility
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    var hasPermissions by remember {
        mutableStateOf(permissionsToRequest.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        })
    }

    fun checkBluetoothAndProceed() {
        if (hasPermissions && bluetoothEnabled) {
            onAllReady()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.all { it.value }
        if (hasPermissions) {
            showError = null
            checkBluetoothAndProceed()
        } else {
            showError = "Permissions (including Location) are required for BLE discovery"
        }
    }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        bluetoothEnabled = bluetoothAdapter?.isEnabled == true
        if (bluetoothEnabled) {
            showError = null
            checkBluetoothAndProceed()
        } else {
            showError = "Bluetooth must be enabled"
        }
    }

    LaunchedEffect(Unit) {
        if (hasPermissions && bluetoothEnabled) {
            onAllReady()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!hasBluetooth) {
            Text("Bluetooth Not Available", style = MaterialTheme.typography.headlineMedium)
        } else {
            Text("Bluetooth & Location Permissions", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Text("BlueMesh needs Bluetooth and Location to find nearby devices.", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))

            showError?.let { error ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(text = error, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!hasPermissions) {
                Button(onClick = { permissionLauncher.launch(permissionsToRequest) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Grant Permissions")
                }
            } else if (!bluetoothEnabled) {
                Button(onClick = {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBluetoothLauncher.launch(enableBtIntent)
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Enable Bluetooth")
                }
            }
        }
    }
}
