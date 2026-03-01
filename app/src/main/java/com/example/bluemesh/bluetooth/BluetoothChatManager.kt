package com.example.bluemesh.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

/**
 * Manages Bluetooth connection (client + server), state, and chat send/receive.
 */
private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1200-8000-00805F9B34FB")

data class ChatMessage(
    val text: String,
    val fromMe: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class ConnectionState {
    data object Idle : ConnectionState()
    data object Connecting : ConnectionState()
    data object Listening : ConnectionState()
    data class Connected(val deviceName: String?, val deviceAddress: String) : ConnectionState()
    data class Failed(val message: String?) : ConnectionState()
    data object Disconnected : ConnectionState()
}

@SuppressLint("MissingPermission")
class BluetoothChatManager(private val context: Context) {

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private var socket: BluetoothSocket? = null
    private var connectThread: Thread? = null
    private var serverThread: Thread? = null
    private var readerThread: Thread? = null
    private var serverSocket: BluetoothServerSocket? = null

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) {
                    _discoveredDevices.update {
                        if (device in it) it else it + device
                    }
                }
            }
        }
    }

    fun startDiscovery() {
        if (adapter == null || !adapter.isEnabled) return
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(discoveryReceiver, filter)
        adapter.startDiscovery()
    }

    fun stopDiscovery() {
        if (adapter == null || !adapter.isEnabled) return
        adapter.cancelDiscovery()
        try {
            context.unregisterReceiver(discoveryReceiver)
        } catch (_: IllegalArgumentException) {
            // Receiver not registered
        }
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        if (adapter == null || !adapter.isEnabled) return emptyList()
        return adapter.bondedDevices.toList()
    }

    fun connect(device: BluetoothDevice) {
        disconnect()
        connectThread = Thread {
            try {
                _connectionState.value = ConnectionState.Connecting
                val s = device.createRfcommSocketToServiceRecord(SPP_UUID)
                s.connect()
                setSocketAndStartReader(s, device.name ?: "Unknown", device.address)
            } catch (e: IOException) {
                _connectionState.value = ConnectionState.Failed(e.message ?: "Connection failed")
            }
        }.apply { start() }
    }

    fun startServer() {
        if (adapter == null || !adapter.isEnabled) return
        stopServer()
        serverThread = Thread {
            try {
                val ss = adapter.listenUsingRfcommWithServiceRecord("BlueMesh", SPP_UUID)
                serverSocket = ss
                _connectionState.value = ConnectionState.Listening
                val clientSocket = ss.accept()
                serverSocket = null
                val device = clientSocket.remoteDevice
                setSocketAndStartReader(clientSocket, device.name ?: "Unknown", device.address)
            } catch (e: IOException) {
                if (serverSocket != null) {
                    _connectionState.value = ConnectionState.Failed(e.message ?: "Accept failed")
                }
            }
        }.apply { start() }
    }

    fun stopServer() {
        try {
            serverSocket?.close()
        } catch (_: IOException) {}
        serverSocket = null
        serverThread?.interrupt()
        serverThread = null
        if (_connectionState.value is ConnectionState.Listening) {
            _connectionState.value = ConnectionState.Idle
        }
    }

    private fun setSocketAndStartReader(s: BluetoothSocket, deviceName: String, deviceAddress: String) {
        socket = s
        _connectionState.value = ConnectionState.Connected(deviceName, deviceAddress)
        startReaderThread()
    }

    private fun startReaderThread() {
        readerThread = Thread {
            try {
                val input = socket?.inputStream ?: return@Thread
                val reader = BufferedReader(InputStreamReader(input))
                while (socket?.isConnected == true) {
                    val line = reader.readLine() ?: break
                    if (line.isBlank()) continue
                    _messages.value = _messages.value + ChatMessage(text = line, fromMe = false)
                }
            } catch (_: IOException) {
                // Connection closed
            } finally {
                if (_connectionState.value is ConnectionState.Connected) {
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }.apply { start() }
    }

    fun sendMessage(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        val out = socket?.outputStream ?: return
        try {
            val writer = OutputStreamWriter(out)
            writer.write(t)
            writer.write("\n")
            writer.flush()
            _messages.value = _messages.value + ChatMessage(text = t, fromMe = true)
        } catch (e: IOException) {
            _connectionState.value = ConnectionState.Failed("Send failed: ${e.message}")
        }
    }

    fun disconnect() {
        stopServer()
        connectThread?.interrupt()
        connectThread = null
        readerThread?.interrupt()
        readerThread = null
        try {
            socket?.close()
        } catch (_: IOException) {}
        socket = null
        if (_connectionState.value is ConnectionState.Connected || _connectionState.value is ConnectionState.Connecting) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun release() {
        stopDiscovery()
        disconnect()
    }

    fun getSocket(): BluetoothSocket? = socket
    fun isBluetoothAvailable(): Boolean = adapter != null && adapter.isEnabled
}
