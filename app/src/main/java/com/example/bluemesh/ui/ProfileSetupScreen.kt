package com.example.bluemesh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileSetupScreen(onFinished: (name: String, vibe: String) -> Unit) {
    var name by remember { mutableStateOf("Arjun K.") }
    val vibes = listOf("🦅", "🐦", "🦋", "⚡", "🌿", "🔮")
    var selectedVibe by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "What should\nwe call you?",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 38.sp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "This is shown to nearby devices. You only set this once.",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(0.5f))

        // Large Circle with selected vibe or Question Mark
        Box(
            modifier = Modifier
                .size(140.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                .padding(10.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedVibe ?: "?",
                fontSize = if (selectedVibe != null) 64.sp else 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "PICK A VIBE",
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Vibe Grid
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                vibes.take(5).forEach { vibe ->
                    VibeItem(vibe, isSelected = selectedVibe == vibe) { selectedVibe = vibe }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            VibeItem(vibes.last(), isSelected = selectedVibe == vibes.last()) { selectedVibe = vibes.last() }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Name Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = name,
                onValueChange = { name = it },
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Only visible to people in your mesh.\nNo accounts, no servers — ever.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { if (selectedVibe != null) onFinished(name, selectedVibe!!) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            enabled = name.isNotBlank() && selectedVibe != null,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Enter the mesh",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.Black
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun VibeItem(vibe: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = vibe, fontSize = 24.sp)
    }
}
