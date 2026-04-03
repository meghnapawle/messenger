package com.example.bluemesh.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluemesh.R

@Composable
fun BottomNav(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    // Using Surface with a solid color and navigationBarsPadding to fix the "white zone"
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding() // Handles the system navigation bar area
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("Mesh", R.drawable.globe, currentScreen is Screen.Mesh) { onTabSelected(Screen.Mesh) }
            NavItem("Global", R.drawable.group, currentScreen is Screen.Global) { onTabSelected(Screen.Global) }
            NavItem("Friends", R.drawable.friend, currentScreen is Screen.Friends) { onTabSelected(Screen.Friends) }
            NavItem("Profile", R.drawable.profile, currentScreen is Screen.Profile) { onTabSelected(Screen.Profile) }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: Any,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp)
    ) {
        if (icon is Int) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(30.dp) // Increased size from 24dp
            )
        } else if (icon is String) {
            Text(text = icon, fontSize = 24.sp) // Increased size for emojis too
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
            color = if (isSelected) Color(0xFF2DE0AD) else Color.Gray
        )
    }
}
