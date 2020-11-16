package com.nankai.flutter_nearby_connections

import com.google.gson.annotations.SerializedName

data class DeviceJson(@SerializedName("deviceId") var deviceID: String,
                      @SerializedName("deviceName") var deviceName: String,
                      @SerializedName("state") var state: Int)