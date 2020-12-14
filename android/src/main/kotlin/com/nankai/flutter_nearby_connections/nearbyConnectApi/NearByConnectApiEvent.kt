package com.nankai.flutter_nearby_connections.nearbyConnectApi

import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.nankai.flutter_nearby_connections.device.DeviceModel
import com.nankai.flutter_nearby_connections.INVOKE_MESSAGE_RECEIVE_METHOD
import com.nankai.flutter_nearby_connections.NearbyService
import com.nankai.flutter_nearby_connections.device.DeviceManager
import com.nankai.flutter_nearby_connections.event.NearbyEvent
import io.flutter.plugin.common.MethodChannel

const val CONNECTING = 1
const val CONNECTED = 2
const val NOT_CONNECTED = 3

class NearByConnectApiEvent(private val channel: MethodChannel,
                            private val serviceType: String,
                            private val deviceManager: DeviceManager,
                            private val service: NearbyService)
    : NearbyEvent {

    private val TAG = "NearByConnectApiEvent"

    private var connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(service)

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String,
                                     discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.d(TAG, "onEndpointFound $discoveredEndpointInfo")
            if (!deviceManager.deviceExists(endpointId)) {
                val data = DeviceModel(endpointId, discoveredEndpointInfo.endpointName, NOT_CONNECTED)
                deviceManager.addDevice(data)
            }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "onEndpointLost $endpointId")
            if (deviceManager.deviceExists(endpointId)) {
                Nearby.getConnectionsClient(service).disconnectFromEndpoint(endpointId)
            }
            deviceManager.removeDevice(endpointId)
        }
    }

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.d(TAG, "onPayloadReceived $endpointId")
            val args = mutableMapOf("deviceId" to endpointId, "message" to String(payload.asBytes()!!))
            channel.invokeMethod(INVOKE_MESSAGE_RECEIVE_METHOD, args)
        }

        override fun onPayloadTransferUpdate(endpointId: String,
                                             payloadTransferUpdate: PayloadTransferUpdate) {
            // required for files and streams
            Log.d(TAG, "onPayloadTransferUpdate $endpointId")
        }
    }

    private val connectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d(TAG, "onConnectionInitiated $connectionInfo")
            val data = DeviceModel(endpointId, connectionInfo.endpointName, CONNECTING)
            deviceManager.addDevice(data)
            Nearby.getConnectionsClient(service).acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Log.d(TAG, "onConnectionResult $endpointId")
            val data = if (result.status.isSuccess) {
                DeviceModel(endpointId,
                        if (deviceManager.device(endpointId)?.deviceName == null) "Null" else deviceManager.device(endpointId)?.deviceName!!, CONNECTED)
            } else {
                DeviceModel(endpointId,
                        if (deviceManager.device(endpointId)?.deviceName == null) "Null" else deviceManager.device(endpointId)?.deviceName!!, NOT_CONNECTED)
            }
            service.notificationUpdate("Connecting to ${data.deviceName}")
            deviceManager.addDevice(data)
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "onDisconnected $endpointId")
            service.notificationUpdate("$serviceType is running!")
            if (deviceManager.deviceExists(endpointId)) {
                deviceManager.updateStatus(endpointId, NOT_CONNECTED)
            } else {
                val data = DeviceModel(endpointId, if (deviceManager.device(endpointId)?.deviceName == null) "Null" else deviceManager.device(endpointId)?.deviceName!!, NOT_CONNECTED)
                deviceManager.addDevice(data)
            }
        }
    }

    /**
     * Mapping events
     */

    override fun startAdvertising(deviceName: String, serviceId: String, build: AdvertisingOptions) {
        Log.d(TAG, "Start advertising")
        connectionsClient.startAdvertising(deviceName, serviceId, connectionLifecycleCallback, build)
    }

    override fun startDiscovery(serviceId: String, build: DiscoveryOptions) {
        Log.d(TAG, "Start discovery")
        connectionsClient.startDiscovery(serviceId, endpointDiscoveryCallback, build)
    }

    override fun requestConnection(endpointId: String, displayName: String) {
        Log.d(TAG, "Request connection")
        connectionsClient.requestConnection(displayName, endpointId, connectionLifecycleCallback)
    }

    override fun stopAdvertising() {
        Log.d(TAG, "Stop advertising")
        connectionsClient.stopAdvertising()
    }

    override fun stopDiscovery() {
        Log.d(TAG, "Stop discovery")
        connectionsClient.stopDiscovery()
    }

    override fun stopAllEndpoints() {
        Log.d(TAG, "Stop all endpoints")
        connectionsClient.stopAllEndpoints()
    }

    override fun disconnectFromEndpoint(endpointId: String) {
        Log.d(TAG, "Disconnect from endpoint")
        connectionsClient.disconnectFromEndpoint(endpointId)
        deviceManager.updateStatus(endpointId, NOT_CONNECTED)
    }

    override fun sendPayload(endpointId: String, fromBytes: Payload) {
        Log.d(TAG, "Send payload")
        connectionsClient.sendPayload(endpointId, fromBytes)
    }

    override fun onDispose() {
        Log.d(TAG, "Dispose")
        stopAdvertising()
        stopDiscovery()
        stopAllEndpoints()
    }
}
