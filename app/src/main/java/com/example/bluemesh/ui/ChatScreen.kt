package com.example.bluemesh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bluemesh.bluetooth.BluetoothChatManager
import com.example.bluemesh.bluetooth.ChatMessage
import com.example.bluemesh.bluetooth.ConnectionState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    bluetoothManager: BluetoothChatManager,
    deviceName: String,
    deviceAddress: String,
    onBack: () -> Unit
) {
    val messages by bluetoothManager.messages.collectAsState()
    val connectionState by bluetoothManager.connectionState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Chat with $deviceName")
                            Spacer(modifier = Modifier.width(8.dp))
                            // Using deviceAddress here to fix the unused parameter warning
                            Text(
                                text = "[$deviceAddress]",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = when (val state = connectionState) {
                                is ConnectionState.Connected -> "Connected to ${state.deviceName}"
                                is ConnectionState.Connecting -> "Connecting…"
                                is ConnectionState.Listening -> "Listening…"
                                is ConnectionState.Disconnected, is ConnectionState.Idle -> "Disconnected"
                                is ConnectionState.Failed -> "Error: ${state.message}"
                                is ConnectionState.WaitingForResponse -> "Waiting for acceptance..."
                                is ConnectionState.IncomingRequest -> "Incoming Request..."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when (connectionState) {
                                is ConnectionState.Connected -> MaterialTheme.colorScheme.primary
                                is ConnectionState.Failed -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { "${it.timestamp}-${it.text}-${it.fromMe}" }) { msg ->
                    MessageBubble(message = msg)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") },
                    singleLine = true,
                    maxLines = 3
                )
                Button(
                    onClick = {
                        bluetoothManager.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && (connectionState is ConnectionState.Connected)
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isFromMe = message.fromMe
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isFromMe) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isFromMe) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = if (isFromMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
