package com.example.bluemesh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {

    var page by remember { mutableStateOf(0) }

    val titles = listOf(
        "Bluetooth Mesh Chat",
        "No Internet Needed",
        "Multi-hop Messaging",
        "Private & Secure"
    )

    val descriptions = listOf(
        "Chat with nearby devices using Bluetooth mesh networking.",
        "Messages travel device-to-device without mobile data.",
        "Your message hops across phones to reach distant users.",
        "Only nearby participants can see your messages."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(80.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titles[page],
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = descriptions[page],
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Button(
                onClick = { onFinish() }
            ) {
                Text("Skip")
            }

            Button(
                onClick = {
                    if (page < 3) page++
                    else onFinish()
                }
            ) {
                Text(if (page < 3) "Next" else "Done")
            }
        }
    }
}
