package com.nankai.flutter_nearby_connections

import com.google.gson.annotations.SerializedName

data class DeviceJson(@SerializedName("deviceID") var deviceID: String,
                      @SerializedName("displayName") var displayName: String,
                      @SerializedName("state") var state: Int)