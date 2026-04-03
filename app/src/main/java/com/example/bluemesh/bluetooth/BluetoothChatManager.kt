package com.example.bluemesh.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

private val SERVICE_UUID: UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
private val CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")

// Handshake signals
private const val SIG_REQ = "__REQ__:"
private const val SIG_ACC = "__ACC__"
private const val SIG_REJ = "__REJ__"

data class ChatMessage(
    val text: String,
    val fromMe: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class ConnectionState {
    data object Idle : ConnectionState()
    data object Connecting : ConnectionState()
    data object Listening : ConnectionState()
    data object WaitingForResponse : ConnectionState()
    data class IncomingRequest(val device: BluetoothDevice, val name: String) : ConnectionState()
    data class Connected(val deviceName: String?, val deviceAddress: String) : ConnectionState()
    data class Failed(val message: String?) : ConnectionState()
    data object Disconnected : ConnectionState()
}

class BluetoothChatManager(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _userName = MutableStateFlow("Arjun K.")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userVibe = MutableStateFlow("🐦")
    val userVibe: StateFlow<String> = _userVibe.asStateFlow()

    private var bluetoothGatt: BluetoothGatt? = null
    private var gattServer: BluetoothGattServer? = null
    private var connectedDevice: BluetoothDevice? = null
    
    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("MissingPermission")
    fun updateProfile(name: String, vibe: String) {
        _userName.value = name
        _userVibe.value = vibe
        if (checkConnectPermission()) {
            adapter?.name = name
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAdvertisePermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) hasPermission(Manifest.permission.BLUETOOTH_ADVERTISE) else true
    private fun checkScanPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) hasPermission(Manifest.permission.BLUETOOTH_SCAN) else true
    private fun checkConnectPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) hasPermission(Manifest.permission.BLUETOOTH_CONNECT) else true

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            _discoveredDevices.update { list ->
                if (list.any { it.address == device.address }) list else list + device
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (adapter == null || !adapter.isEnabled || !checkScanPermission()) return
        _discoveredDevices.value = emptyList()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        adapter.bluetoothLeScanner?.startScan(null, settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        if (adapter == null || !adapter.isEnabled || !checkScanPermission()) return
        adapter.bluetoothLeScanner?.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        if (!checkConnectPermission()) return
        disconnect()
        _connectionState.value = ConnectionState.Connecting
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = ConnectionState.Disconnected
                connectedDevice = null
                bluetoothGatt = null
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    if (descriptor != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        } else {
                            @Suppress("DEPRECATION")
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                    // Wait for descriptor write then send Request
                    mainHandler.postDelayed({
                        sendSignal(SIG_REQ + _userName.value)
                        _connectionState.value = ConnectionState.WaitingForResponse
                    }, 500)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            handleIncomingData(String(value), gatt.device)
        }

        @Deprecated("Deprecated")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            handleIncomingData(String(characteristic.value), gatt.device)
        }
    }

    @SuppressLint("MissingPermission")
    fun startServer() {
        if (adapter == null || !adapter.isEnabled || !checkAdvertisePermission() || !checkConnectPermission()) return
        if (adapter.name != _userName.value) { adapter.name = _userName.value }
        stopServer()
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(characteristic)
        gattServer?.addService(service)
        
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()
        val data = AdvertiseData.Builder().addServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        val scanResponse = AdvertiseData.Builder().setIncludeDeviceName(true).build()
        adapter.bluetoothLeAdvertiser?.startAdvertising(settings, data, scanResponse, advertiseCallback)
        _connectionState.value = ConnectionState.Listening
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (connectedDevice?.address == device.address) {
                    _connectionState.value = ConnectionState.Disconnected
                    connectedDevice = null
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(device: BluetoothDevice, requestId: Int, characteristic: BluetoothGattCharacteristic, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray) {
            handleIncomingData(String(value), device)
            if (responseNeeded) gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleIncomingData(data: String, device: BluetoothDevice) {
        when {
            data.startsWith(SIG_REQ) -> {
                val name = data.removePrefix(SIG_REQ)
                _connectionState.value = ConnectionState.IncomingRequest(device, name)
            }
            data == SIG_ACC -> {
                connectedDevice = device
                _connectionState.value = ConnectionState.Connected(device.name ?: "Peer", device.address)
            }
            data == SIG_REJ -> {
                _connectionState.value = ConnectionState.Failed("Connection Declined")
                disconnect()
            }
            else -> {
                _messages.update { it + ChatMessage(data, false) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun acceptConnection() {
        val state = _connectionState.value
        if (state is ConnectionState.IncomingRequest) {
            // If already connected to someone else, this implicitly ends that
            connectedDevice = state.device
            sendSignal(SIG_ACC)
            _connectionState.value = ConnectionState.Connected(state.name, state.device.address)
        }
    }

    @SuppressLint("MissingPermission")
    fun declineConnection() {
        val state = _connectionState.value
        if (state is ConnectionState.IncomingRequest) {
            sendSignal(SIG_REJ)
            // No need to call disconnect() here as we haven't officially "connected" in app state
            // But we might want to clear physical connection if gatt server holds it
            _connectionState.value = ConnectionState.Listening
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendSignal(signal: String) {
        val gatt = bluetoothGatt
        if (gatt != null) {
            val service = gatt.getService(SERVICE_UUID)
            val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
            if (characteristic != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeCharacteristic(characteristic, signal.toByteArray(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                } else {
                    @Suppress("DEPRECATION")
                    characteristic.value = signal.toByteArray()
                    gatt.writeCharacteristic(characteristic)
                }
            }
        } else if (gattServer != null) {
            val target = (connectionState.value as? ConnectionState.IncomingRequest)?.device ?: connectedDevice
            val service = gattServer?.getService(SERVICE_UUID)
            val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
            if (characteristic != null && target != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gattServer?.notifyCharacteristicChanged(target, characteristic, false, signal.toByteArray())
                } else {
                    @Suppress("DEPRECATION")
                    characteristic.value = signal.toByteArray()
                    gattServer?.notifyCharacteristicChanged(target, characteristic, false)
                }
            }
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {}
        override fun onStartFailure(errorCode: Int) {}
    }

    @SuppressLint("MissingPermission")
    fun stopServer() {
        if (checkAdvertisePermission()) adapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close()
        gattServer = null
        if (_connectionState.value is ConnectionState.Listening) _connectionState.value = ConnectionState.Idle
    }

    @SuppressLint("MissingPermission")
    fun sendMessage(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        val gatt = bluetoothGatt
        if (gatt != null) {
            val service = gatt.getService(SERVICE_UUID)
            val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
            if (characteristic != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeCharacteristic(characteristic, t.toByteArray(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                } else {
                    @Suppress("DEPRECATION")
                    characteristic.value = t.toByteArray()
                    gatt.writeCharacteristic(characteristic)
                }
                _messages.update { it + ChatMessage(t, true) }
            }
        } else if (gattServer != null && connectedDevice != null) {
            val service = gattServer?.getService(SERVICE_UUID)
            val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
            if (characteristic != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gattServer?.notifyCharacteristicChanged(connectedDevice!!, characteristic, false, t.toByteArray())
                } else {
                    @Suppress("DEPRECATION")
                    characteristic.value = t.toByteArray()
                    gattServer?.notifyCharacteristicChanged(connectedDevice, characteristic, false)
                }
                _messages.update { it + ChatMessage(t, true) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        if (checkConnectPermission()) {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        }
        bluetoothGatt = null
        connectedDevice = null
        _connectionState.value = ConnectionState.Disconnected
    }

    fun clearMessages() { _messages.value = emptyList() }
    fun release() { stopDiscovery(); disconnect(); stopServer() }
    fun isBluetoothAvailable(): Boolean = adapter != null && adapter.isEnabled
}
