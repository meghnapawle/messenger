package com.example.bluemesh.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNav(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("Mesh", "🌐", currentScreen is Screen.Mesh) { onTabSelected(Screen.Mesh) }
            NavItem("Groups", "🏠", currentScreen is Screen.Groups) { onTabSelected(Screen.Groups) }
            NavItem("Friends", "👥", currentScreen is Screen.Friends) { onTabSelected(Screen.Friends) }
            NavItem("Profile", "👤", currentScreen is Screen.Profile) { onTabSelected(Screen.Profile) }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: String,
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
            .padding(8.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF2DE0AD) else Color.Gray
        )
    }
}
