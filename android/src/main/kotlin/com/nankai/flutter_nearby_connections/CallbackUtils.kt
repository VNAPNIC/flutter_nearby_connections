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

    val advertConnectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d("nearby_connections", "onConnectionInitiated")
            val data = DeviceJson(endpointId, connectionInfo.endpointName, 3)
            addDevice(data)
        }

        override fun onConnectionResult(endpointId: String, connectionResolution: ConnectionResolution) {
            Log.d("nearby_connections", "onConnectionResult")
            val data = DeviceJson(endpointId, if (device(endpointId)?.displayName == null) "Null" else device(endpointId)?.displayName!!, when (connectionResolution.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> 2
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> 3
                ConnectionsStatusCodes.STATUS_ERROR -> 3
                else -> 3
            })
            addDevice(data)
        }

        override fun onDisconnected(endpointId: String) {
            Log.d("nearby_connections", "onDisconnected")
            val data = DeviceJson(endpointId, if (device(endpointId)?.displayName == null) "Null" else device(endpointId)?.displayName!!, 3)
            addDevice(data)
        }
    }

    val endpointDiscoveryCallback: EndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String,
                                     discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.d("nearby_connections", "onEndpointFound")
            val data = DeviceJson(endpointId, discoveredEndpointInfo.endpointName, 1)
            addDevice(data)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d("nearby_connections", "onEndpointLost")
            val data = DeviceJson(endpointId, if (device(endpointId)?.displayName == null) "Null" else device(endpointId)?.displayName!!, 3)
            addDevice(data)
        }
    }

    val discoverConnectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d("nearby_connections", "onConnectionInitiated")
            val data = DeviceJson(endpointId, connectionInfo.endpointName, 3)
            addDevice(data)
        }

        override fun onConnectionResult(endpointId: String, connectionResolution: ConnectionResolution) {
            Log.d("nearby_connections", "onConnectionResult")
            val data = DeviceJson(endpointId, if (device(endpointId)?.displayName == null) "Null" else device(endpointId)?.displayName!!, when (connectionResolution.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> 2
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> 3
                ConnectionsStatusCodes.STATUS_ERROR -> 3
                else -> 3
            })
            addDevice(data)
        }

        override fun onDisconnected(endpointId: String) {
            Log.d("nearby_connections", "onDisconnected")
            val data = DeviceJson(endpointId, if (device(endpointId)?.displayName == null) "Null" else device(endpointId)?.displayName!!, 3)
            addDevice(data)
        }
    }

    val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.d("nearby_connections", "onPayloadReceived")
            val args: MutableMap<String, Any?> = HashMap()
            args["endpointId"] = endpointId
            args["payloadId"] = payload.id
            args["type"] = payload.type
            if (payload.type == Payload.Type.BYTES) {
                val bytes = payload.asBytes()!!
                args["bytes"] = bytes
            } else if (payload.type == Payload.Type.FILE) {
                args["filePath"] = payload.asFile()!!.asJavaFile()!!.absolutePath
            }
            channel.invokeMethod("onPayloadReceived", args)
        }

        override fun onPayloadTransferUpdate(endpointId: String,
                                             payloadTransferUpdate: PayloadTransferUpdate) {
            // required for files and streams
            Log.d("nearby_connections", "onPayloadTransferUpdate")
            val args: MutableMap<String, Any> = HashMap()
            args["endpointId"] = endpointId
            args["payloadId"] = payloadTransferUpdate.payloadId
            args["status"] = payloadTransferUpdate.status
            args["bytesTransferred"] = payloadTransferUpdate.bytesTransferred
            args["totalBytes"] = payloadTransferUpdate.totalBytes
            channel.invokeMethod("onPayloadTransferUpdate", args)
        }
    }
}
