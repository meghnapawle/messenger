package com.example.bluemesh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable // Added
import androidx.compose.foundation.interaction.MutableInteractionSource // Added
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // Added
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed // Added
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluemesh.bluetooth.BluetoothChatManager

@Composable
fun ProfileScreen(
    bluetoothManager: BluetoothChatManager,
    onEditProfile: () -> Unit,
    onTabSelected: (Screen) -> Unit
) {
    val userName by bluetoothManager.userName.collectAsState()
    val userVibe by bluetoothManager.userVibe.collectAsState()

    Scaffold(
        bottomBar = { BottomNav(Screen.Profile, onTabSelected) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Large Profile Picture (Vibe)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2DE0AD).copy(alpha = 0.1f))
                    .border(2.dp, Color(0xFF2DE0AD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = userVibe, fontSize = 80.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = userName,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Broadcasting on BlueMesh",
                fontSize = 16.sp,
                color = Color(0xFF2DE0AD)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Settings List
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileOption("Edit Name & Vibe", "✏️", onEditProfile)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileOption("Privacy Settings", "🔒") {}
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileOption("About BlueMesh", "ℹ️") {}
                }
            }
        }
    }
}

@Composable
fun ProfileOption(label: String, icon: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "›", color = Color.Gray, fontSize = 24.sp)
    }
}

// Added this extension function to fix the Unresolved Reference error
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}