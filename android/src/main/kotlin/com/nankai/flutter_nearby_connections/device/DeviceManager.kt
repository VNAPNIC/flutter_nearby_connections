package com.nankai.flutter_nearby_connections.device

import com.google.gson.Gson
import com.nankai.flutter_nearby_connections.INVOKE_CHANGE_STATE_METHOD
import io.flutter.plugin.common.MethodChannel

class DeviceManager(private val channel: MethodChannel) {
    private val gson: Gson = Gson()

    private val devices = mutableListOf<DeviceModel>()

    fun deviceExists(deviceId: String) = devices.any { element -> element.deviceID == deviceId }
    fun device(deviceId: String): DeviceModel? = devices.find { element -> element.deviceID == deviceId }
    fun addDevice(device: DeviceModel) {
        if (deviceExists(device.deviceID)) {
            updateStatus(device.deviceID, device.state)
        } else {
            devices.add(device)
        }
        val json = gson.toJson(devices)
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, json)
    }

    fun removeDevice(deviceId: String) {
        devices.remove(device(deviceId))
        val json = gson.toJson(devices)
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, json)
    }

    fun updateStatus(deviceId: String, state: Int) {
        devices.find { element -> element.deviceID == deviceId }?.state = state
        val json = gson.toJson(devices)
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, json)
    }

    fun clear(){
        devices.clear()
        val json = gson.toJson(devices)
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, json)
    }
}