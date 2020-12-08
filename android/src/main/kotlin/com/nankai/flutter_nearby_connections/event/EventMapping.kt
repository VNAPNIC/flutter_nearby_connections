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

    private var state = BluetoothAdapter.STATE_OFF
    private var bluetoothExist = false

    private lateinit var nearByConnectApiEvent: NearByConnectApiEvent
    private lateinit var wifiP2PEvent: WifiP2PEvent

    private val bluetoothChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> Log.i(TAG, "Bluetooth off")
                    BluetoothAdapter.STATE_TURNING_OFF -> Log.i(TAG, "Turning Bluetooth off...")
                    BluetoothAdapter.STATE_ON -> Log.i(TAG, "Bluetooth on")
                    BluetoothAdapter.STATE_TURNING_ON -> Log.i(TAG, "Turning Bluetooth on...")
                }
            }
        }
    }

    private fun logBluetooth() {
        Log.i(TAG, when (state) {
            BluetoothAdapter.STATE_OFF -> "Bluetooth off"
            BluetoothAdapter.STATE_TURNING_OFF -> "Turning Bluetooth off..."
            BluetoothAdapter.STATE_ON -> "Bluetooth on"
            BluetoothAdapter.STATE_TURNING_ON -> "Turning Bluetooth on..."
            else -> "Bluetooth off"
        })
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
        stopAdvertising()
        stopDiscovery()
        wifiP2PEvent.stopAllEndpoints()
    }

    override fun startAdvertising(strategy: Strategy, deviceName: String) {
        wifiP2PEvent.startAdvertising(
                deviceName,
                SERVICE_ID,
                AdvertisingOptions.Builder().setStrategy(strategy).build())
    }

    override fun startDiscovery(strategy: Strategy, deviceName: String) {
        wifiP2PEvent.startDiscovery(
                SERVICE_ID,
                deviceName,
                DiscoveryOptions.Builder().setStrategy(strategy).build())
    }

    override fun connect(endpointId: String, displayName: String) {
        wifiP2PEvent.requestConnection(endpointId, displayName)
    }

    override fun stopDiscovery() {
        wifiP2PEvent.stopDiscovery()
    }

    override fun stopAdvertising() {
        wifiP2PEvent.stopAdvertising()
    }

    override fun disconnect(endpointId: String) {
        wifiP2PEvent.disconnectFromEndpoint(endpointId)
    }

    override fun sendStringPayload(endpointId: String, str: String) {
        wifiP2PEvent.sendPayload(endpointId, Payload.fromBytes(str.toByteArray()))
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

}