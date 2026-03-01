package com.example.bluemesh.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bluemesh.bluetooth.BluetoothChatManager

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDiscoveryScreen(
    bluetoothManager: BluetoothChatManager,
    onDeviceSelected: (deviceName: String, deviceAddress: String) -> Unit,
    onBack: () -> Unit
) {
    val discoveredDevices by bluetoothManager.discoveredDevices.collectAsState()
    var isDiscovering by remember { mutableStateOf(false) }

    val pairedDevices = remember { bluetoothManager.getPairedDevices() }

    val discoverableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* Nothing to do with result */ }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDiscovering = true
                bluetoothManager.startDiscovery()
                bluetoothManager.startServer()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                isDiscovering = false
                bluetoothManager.stopDiscovery()
                bluetoothManager.stopServer()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover Devices") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                    }
                    discoverableLauncher.launch(intent)
                }) {
                Text("Make this device discoverable")
            }
            Spacer(modifier = Modifier.height(16.dp))
            DeviceListSection(
                header = "Paired Devices",
                devices = pairedDevices,
                onDeviceClick = { device ->
                    onDeviceSelected(device.name ?: "Unknown", device.address)
                }
            )

            DeviceListSection(
                header = "Available Devices",
                devices = discoveredDevices.filter { it !in pairedDevices },
                onDeviceClick = { device ->
                    onDeviceSelected(device.name ?: "Unknown", device.address)
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDiscovering) {
                    CircularProgressIndicator()
                    Text("Scanning...", modifier = Modifier.padding(start = 8.dp))
                } else {
                    Button(onClick = {
                        isDiscovering = true
                        bluetoothManager.startDiscovery()
                    }) {
                        Text("Scan for devices")
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DeviceListSection(
    header: String,
    devices: List<BluetoothDevice>,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = header, style = MaterialTheme.typography.titleMedium)
        if (devices.isEmpty()) {
            Text(
                text = "No devices found.",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyColumn {
                items(devices) { device ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDeviceClick(device) }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(text = "${device.name ?: "Unknown Device"}\n${device.address}")
                    }
                }
            }
        }
    }
}
