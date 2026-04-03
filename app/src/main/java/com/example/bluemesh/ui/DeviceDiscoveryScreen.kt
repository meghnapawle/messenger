package com.example.bluemesh.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bluemesh.bluetooth.BluetoothChatManager

@SuppressLint("MissingPermission")
@Composable
fun DeviceDiscoveryScreen(
    bluetoothManager: BluetoothChatManager,
    onDeviceSelected: (deviceName: String, deviceAddress: String) -> Unit,
    onEditProfile: () -> Unit,
    onTabSelected: (Screen) -> Unit
) {
    val discoveredDevices by bluetoothManager.discoveredDevices.collectAsState()
    val userName by bluetoothManager.userName.collectAsState()
    val userVibe by bluetoothManager.userVibe.collectAsState()
    var isDiscovering by remember { mutableStateOf(false) }

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
        bottomBar = { BottomNav(Screen.Mesh, onTabSelected) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mesh",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2DE0AD))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active — ${discoveredDevices.size} nearby",
                            fontSize = 14.sp,
                            color = Color(0xFF2DE0AD)
                        )
                    }
                }
                IconButton(
                    onClick = { /* Search */ },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Profile Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditProfile() },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2DE0AD).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2DE0AD).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF2DE0AD), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = userVibe, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "(you)", fontSize = 14.sp, color = Color.Gray)
                        }
                        Text(text = "Broadcasting • Node hub", fontSize = 14.sp, color = Color(0xFF2DE0AD))
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2DE0AD).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "ONLINE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2DE0AD)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scanning Status Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDiscovering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF2DE0AD)
                        )
                    } else {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Gray))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isDiscovering) "Scanning for nearby devices..." else "Scanner idle",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "NEARBY · ${discoveredDevices.size} DEVICES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Discovery List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(discoveredDevices) { device ->
                    DeviceCard(
                        name = device.name ?: "Unknown device",
                        initials = (device.name ?: "??").take(2).uppercase(),
                        onConnect = { onDeviceSelected(device.name ?: "Unknown", device.address) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceCard(name: String, initials: String, onConnect: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initials, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Signal: Good", fontSize = 14.sp, color = Color.Gray)
            }
            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DE0AD)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "Connect", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
