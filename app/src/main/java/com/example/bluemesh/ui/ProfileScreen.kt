package com.example.bluemesh.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluemesh.R
import com.example.bluemesh.bluetooth.BluetoothChatManager

@Composable
fun ProfileScreen(
    bluetoothManager: BluetoothChatManager,
    onEditProfile: () -> Unit,
    onTabSelected: (Screen) -> Unit
) {
    val userName by bluetoothManager.userName.collectAsState()
    val userVibe by bluetoothManager.userVibe.collectAsState()

    // Helper to get drawable ID from "avatarX" string
    val userAvatarResId = remember(userVibe) {
        val idx = userVibe.removePrefix("avatar").toIntOrNull() ?: 1
        when(idx) {
            1 -> R.drawable.avatar1
            2 -> R.drawable.avatar2
            3 -> R.drawable.avatar3
            4 -> R.drawable.avatar4
            5 -> R.drawable.avatar5
            6 -> R.drawable.avatar6
            7 -> R.drawable.avatar7
            8 -> R.drawable.avatar8
            9 -> R.drawable.avatar9
            10 -> R.drawable.avatar10
            else -> R.drawable.avatar1
        }
    }

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

            // Large Profile Picture (Avatar)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2DE0AD).copy(alpha = 0.1f))
                    .border(2.dp, Color(0xFF2DE0AD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = userAvatarResId),
                    contentDescription = "My Avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
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
                    ProfileOption("Edit Name & Avatar", R.drawable.profile, onEditProfile)
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
fun ProfileOption(label: String, icon: Any, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon is Int) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        } else if (icon is String) {
            Text(text = icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "›", color = Color.Gray, fontSize = 24.sp)
    }
}

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}
