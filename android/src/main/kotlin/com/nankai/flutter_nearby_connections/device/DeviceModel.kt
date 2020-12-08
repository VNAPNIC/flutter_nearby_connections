package com.nankai.flutter_nearby_connections.device

import com.google.gson.annotations.SerializedName

data class DeviceModel(@SerializedName("deviceId") var deviceID: String,
                       @SerializedName("deviceName") var deviceName: String,
                       @SerializedName("state") var state: Int)