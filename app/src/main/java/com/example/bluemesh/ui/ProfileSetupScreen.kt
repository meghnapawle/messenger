package com.example.bluemesh.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluemesh.R

@Composable
fun ProfileSetupScreen(onFinished: (name: String, vibe: String) -> Unit) {
    var name by remember { mutableStateOf("Arjun K.") }
    
    val avatarResIds = listOf(
        R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3, R.drawable.avatar4, R.drawable.avatar5,
        R.drawable.avatar6, R.drawable.avatar7, R.drawable.avatar8, R.drawable.avatar9, R.drawable.avatar10
    )
    
    var selectedAvatarIdx by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp)) // Reduced from 40dp to move things up
        
        Text(
            text = "What should\nwe call you?",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 38.sp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16dp
        
        Text(
            text = "This is shown to nearby devices. You only set this once.",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp)) // Reduced from 24dp

        // Large Circle showing selected Avatar
        Box(
            modifier = Modifier
                .size(160.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                .padding(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = avatarResIds[selectedAvatarIdx]),
                contentDescription = "Selected Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp)) // Reduced from 32dp
        
        Text(
            text = "PICK AN AVATAR",
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp)) // Reduced from 20dp

        // Avatar Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(avatarResIds.indices.toList()) { index ->
                AvatarItem(
                    resId = avatarResIds[index],
                    isSelected = selectedAvatarIdx == index,
                    onClick = { selectedAvatarIdx = index }
                )
            }
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

        // Removed the "Only visible to people..." text as requested

        Spacer(modifier = Modifier.height(24.dp)) // Added space since text was removed

        Button(
            onClick = { 
                onFinished(name, "avatar${selectedAvatarIdx + 1}") 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            enabled = name.isNotBlank(),
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
fun AvatarItem(resId: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
