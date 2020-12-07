package com.nankai.flutter_nearby_connections

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import com.nankai.flutter_nearby_connections.nearbyConnectApi.NearByConnectApiUtils
import com.nankai.flutter_nearby_connections.wifip2p.WifiP2PUtils
import io.flutter.plugin.common.MethodChannel

class MappingAction(private val service: NearbyService) {

    private val TAG = "MappingAction"

    private lateinit var nearByConnectApiUtils: NearByConnectApiUtils
    private lateinit var wifiP2pUtils: WifiP2PUtils

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

    fun create(){
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        service.registerReceiver(bluetoothChangeReceiver, filter)
    }

   fun initNearBy( channel: MethodChannel,  serviceType: String ){
       nearByConnectApiUtils = NearByConnectApiUtils(channel, serviceType, service)
       wifiP2pUtils = WifiP2PUtils(channel, serviceType, service)
    }


    fun startAdvertising(strategy: Strategy, deviceName: String) {
        nearByConnectApiUtils.startAdvertising(
                deviceName,
                SERVICE_ID,
                AdvertisingOptions.Builder().setStrategy(strategy).build())
    }

    fun startDiscovery(strategy: Strategy) {
        nearByConnectApiUtils.startDiscovery(
                SERVICE_ID,
                DiscoveryOptions.Builder().setStrategy(strategy).build())
    }

    fun connect(endpointId: String, displayName: String) {
        nearByConnectApiUtils.requestConnection(endpointId, displayName)
    }

    fun stopDiscovery() {
        nearByConnectApiUtils.stopDiscovery()
    }

    fun stopAdvertising() {
        nearByConnectApiUtils.stopAdvertising()
    }

    fun disconnect(endpointId: String) {
        nearByConnectApiUtils.disconnectFromEndpoint(endpointId)
    }

    fun sendStringPayload(endpointId: String, str: String) {
        nearByConnectApiUtils.sendPayload(endpointId, Payload.fromBytes(str.toByteArray()))
    }

    fun onDestroy() {
        service.unregisterReceiver(bluetoothChangeReceiver)
        stopAdvertising()
        stopDiscovery()
        nearByConnectApiUtils.stopAllEndpoints()
    }

}