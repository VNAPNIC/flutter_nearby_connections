package com.nankai.flutter_nearby_connections

import android.util.Log
import com.google.android.gms.nearby.connection.*
import com.google.gson.Gson
import io.flutter.plugin.common.MethodChannel
import java.util.*

class CallbackUtils constructor(private val channel: MethodChannel) {

    private val devices = mutableListOf<DeviceJson>()
    private val gson = Gson()
    private fun deviceExists(deviceId: String) = devices.any { element -> element.deviceID == deviceId }
    private fun device(deviceId: String): DeviceJson? = devices.find { element -> element.deviceID == deviceId }
    private fun updateStatus(deviceId: String, state: Int) {
        devices.find { element -> element.deviceID == deviceId }?.state = state
    }

    fun addDevice(device: DeviceJson) {
        if (deviceExists(device.deviceID)) {
            updateStatus(device.deviceID, device.state)
        } else {
            devices.add(device)
        }
        val json = gson.toJson(devices)
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, json)
    }

    fun removeDevice(deviceId: String){
        devices.remove(device(deviceId))
        val json = gson.toJson(devices)
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, json)
    }

    val advertConnectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d("nearby_connections", "onConnectionInitiated $endpointId")
            val data = DeviceJson(endpointId, connectionInfo.endpointName, 3)
            addDevice(data)
        }

        override fun onConnectionResult(endpointId: String, connectionResolution: ConnectionResolution) {
            Log.d("nearby_connections", "onConnectionResult $endpointId")
            val data = DeviceJson(endpointId,
                    if (device(endpointId)?.deviceName == null) "Null" else device(endpointId)?.deviceName!!,
                    when (connectionResolution.status.statusCode) {
                        ConnectionsStatusCodes.STATUS_OK -> 2
                        ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> 3
                        ConnectionsStatusCodes.STATUS_ERROR -> 3
                        else -> 3
                    })
            addDevice(data)
        }

        override fun onDisconnected(endpointId: String) {
            Log.d("nearby_connections", "onDisconnected $endpointId")
            val data = DeviceJson(endpointId,
                    if (device(endpointId)?.deviceName == null) "Null" else device(endpointId)?.deviceName!!,
                    3)
            addDevice(data)
        }
    }

    val endpointDiscoveryCallback: EndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String,
                                     discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.d("nearby_connections", "onEndpointFound $endpointId")
            val data = DeviceJson(endpointId, discoveredEndpointInfo.endpointName, 3)
            addDevice(data)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d("nearby_connections", "onEndpointLost $endpointId")
            removeDevice(endpointId)
        }
    }

    val discoverConnectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d("nearby_connections", "onConnectionInitiated $endpointId")
            val data = DeviceJson(endpointId, connectionInfo.endpointName, 3)
            addDevice(data)
        }

        override fun onConnectionResult(endpointId: String, connectionResolution: ConnectionResolution) {
            Log.d("nearby_connections", "onConnectionResult $endpointId")
            val data = DeviceJson(endpointId,
                    if (device(endpointId)?.deviceName == null) "Null" else device(endpointId)?.deviceName!!,
                    when (connectionResolution.status.statusCode) {
                        ConnectionsStatusCodes.STATUS_OK -> 2
                        ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> 3
                        ConnectionsStatusCodes.STATUS_ERROR -> 3
                        else -> 3
                    })
            addDevice(data)
        }

        override fun onDisconnected(endpointId: String) {
            Log.d("nearby_connections", "onDisconnected $endpointId")
            val data = DeviceJson(endpointId,
                    if (device(endpointId)?.deviceName == null) "Null" else device(endpointId)?.deviceName!!,
                    3)
            addDevice(data)
        }
    }

    val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.d("nearby_connections", "onPayloadReceived $endpointId")
        }

        override fun onPayloadTransferUpdate(endpointId: String,
                                             payloadTransferUpdate: PayloadTransferUpdate) {
            // required for files and streams
            Log.d("nearby_connections", "onPayloadTransferUpdate $endpointId")
        }
    }
}
