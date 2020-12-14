package com.nankai.flutter_nearby_connections.event

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import com.google.gson.Gson
import com.nankai.flutter_nearby_connections.NearbyService
import com.nankai.flutter_nearby_connections.SERVICE_ID
import com.nankai.flutter_nearby_connections.device.DeviceManager
import com.nankai.flutter_nearby_connections.nearbyConnectApi.NearByConnectApiEvent
import com.nankai.flutter_nearby_connections.wifip2p.WifiP2PEvent
import io.flutter.plugin.common.MethodChannel

class EventMapping(private val service: NearbyService) : NearbyServiceEvent {

    private val TAG = "MappingAction"

    private val gson = Gson()
    private lateinit var deviceManager: DeviceManager

    private var newBluetoothState = -1
    private var bluetoothExist = false
    private var firstStart = true
    private var isStartAdvertising = false
    private var strategy: Strategy? = null
    private var deviceName: String? = null

    private lateinit var nearByConnectApiEvent: NearByConnectApiEvent
    private lateinit var wifiP2PEvent: WifiP2PEvent

    private val bluetoothChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                newBluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR)
                when (newBluetoothState) {
                    BluetoothAdapter.STATE_TURNING_OFF -> Log.i(TAG, "Turning Bluetooth off...")
                    BluetoothAdapter.STATE_OFF -> {
                        Log.i(TAG, "Bluetooth off")
                        restartWifiP2p()
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> Log.i(TAG, "Turning Bluetooth on...")
                    BluetoothAdapter.STATE_ON -> {
                        Log.i(TAG, "Bluetooth on")
                        restartNearbyConnection()
                    }
                }
            }
        }
    }

    private fun restartWifiP2p() {
        bluetoothLog()

        wifiP2PEvent.unregisterReceiver()
        if (isStartAdvertising) {
            if (strategy != null && deviceName != null) {
                stopAdvertising(isWifiP2p = true)
                restartAdvertising(isWifiP2p = true)
            }
        }

        stopDiscovery(isWifiP2p = true)
        stopAllEndpoints(isWifiP2p = true)
        startDiscovery(isWifiP2p = true)
    }

    private fun restartNearbyConnection() {
        bluetoothLog()

        wifiP2PEvent.unregisterReceiver()
        if (isStartAdvertising) {
            if (strategy != null && deviceName != null) {
                stopAdvertising(isWifiP2p = false)
                restartAdvertising(isWifiP2p = false)
            }
        }

        stopDiscovery(isWifiP2p = false)
        stopAllEndpoints(isWifiP2p = false)
        startDiscovery(isWifiP2p = false)
    }


    private fun bluetoothLog() {
        Log.i(TAG, "\n【Reset connect with info】" +
                "\nNew bluetooth state: $newBluetoothState" +
                "\nBluetooth exist: $bluetoothExist" +
                "\nStrategy: $strategy" +
                "\nDevice name: $deviceName")
    }


    override fun onCreate() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        service.registerReceiver(bluetoothChangeReceiver, filter)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothExist = service.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        }

        if (!bluetoothExist) {
            bluetoothExist = service.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        }

        Log.i(TAG, "Bluetooth exist on the device is $bluetoothExist")

        if (bluetoothExist) {
            setBluetooth(false)
        }
    }

    override fun onStart(channel: MethodChannel, serviceType: String) {
        deviceManager = DeviceManager(channel)
        nearByConnectApiEvent = NearByConnectApiEvent(channel, serviceType, deviceManager, service)
        wifiP2PEvent = WifiP2PEvent(channel, serviceType, deviceManager, service)
    }

    override fun onDestroy() {
        service.unregisterReceiver(bluetoothChangeReceiver)
        wifiP2PEvent.onDispose()
        nearByConnectApiEvent.onDispose()
    }

    override fun startAdvertising(strategy: Strategy, deviceName: String) {
        this.strategy = strategy
        this.deviceName = deviceName

        if (!bluetoothExist) {
            restartAdvertising(isWifiP2p = true)
        }
    }

    override fun startDiscovery() {
        if (bluetoothExist) {
            setBluetooth(true)
        } else {
            startDiscovery(isWifiP2p = true)
        }
    }

    override fun stopAdvertising() {
        stopAdvertising(isWifiP2p = !bluetoothEnable())
    }

    override fun stopDiscovery() {
        stopAdvertising(isWifiP2p = !bluetoothEnable())
    }

    override fun connect(endpointId: String, displayName: String) {
        if (bluetoothEnable()) {
            nearByConnectApiEvent.requestConnection(endpointId, displayName)
        } else {
            wifiP2PEvent.requestConnection(endpointId, displayName)
        }
    }

    override fun disconnect(endpointId: String) {
        if (bluetoothEnable()) {
            nearByConnectApiEvent.disconnectFromEndpoint(endpointId)
        } else {
            wifiP2PEvent.disconnectFromEndpoint(endpointId)
        }
    }

    override fun sendStringPayload(endpointId: String, str: String) {
        if (bluetoothEnable()) {
            nearByConnectApiEvent.sendPayload(endpointId, Payload.fromBytes(str.toByteArray()))
        } else {
            wifiP2PEvent.sendPayload(endpointId, Payload.fromBytes(str.toByteArray()))
        }
    }

    private fun setBluetooth(enable: Boolean): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val isEnabled = bluetoothAdapter.isEnabled
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable()
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable()
        }
        // No need to change bluetooth state
        return true
    }

    private fun bluetoothEnable(): Boolean = newBluetoothState == BluetoothAdapter.STATE_ON && bluetoothExist

    private fun stopAdvertising(isWifiP2p: Boolean) {
        if (isWifiP2p) {
            nearByConnectApiEvent.stopAllEndpoints()
        } else {
            wifiP2PEvent.stopAllEndpoints()
        }
    }

    private fun stopDiscovery(isWifiP2p: Boolean) {
        if (isWifiP2p) {
            nearByConnectApiEvent.stopAllEndpoints()
        } else {
            wifiP2PEvent.stopAllEndpoints()
        }
    }

    private fun stopAllEndpoints(isWifiP2p: Boolean) {
        if (isWifiP2p) {
            nearByConnectApiEvent.stopAllEndpoints()
        } else {
            wifiP2PEvent.stopAllEndpoints()
        }
    }

    private fun startDiscovery(isWifiP2p: Boolean) {
        if (isWifiP2p) {
            wifiP2PEvent.startDiscovery(
                    SERVICE_ID,
                    DiscoveryOptions.Builder().setStrategy(strategy).build())
        } else {
            nearByConnectApiEvent.startDiscovery(
                    SERVICE_ID,
                    DiscoveryOptions.Builder().setStrategy(strategy).build())
        }
    }

    private fun restartAdvertising(isWifiP2p: Boolean) {
        if (isWifiP2p) {
            wifiP2PEvent.startAdvertising(
                    deviceName!!,
                    SERVICE_ID,
                    AdvertisingOptions.Builder().setStrategy(strategy).build())
        } else {
            nearByConnectApiEvent.startAdvertising(
                    deviceName!!,
                    SERVICE_ID,
                    AdvertisingOptions.Builder().setStrategy(strategy).build())
        }
    }
}