package com.nankai.flutter_nearby_connections.event

import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.Payload

interface NearbyEvent {
    fun startAdvertising(deviceName: String, serviceId: String, build: AdvertisingOptions)

    fun startDiscovery(serviceId: String, deviceName: String, build: DiscoveryOptions)

    fun requestConnection(endpointId: String, displayName: String)

    fun stopDiscovery()

    fun stopAdvertising()

    fun stopAllEndpoints()

    fun disconnectFromEndpoint(endpointId: String)

    fun sendPayload(endpointId: String, fromBytes: Payload)
}