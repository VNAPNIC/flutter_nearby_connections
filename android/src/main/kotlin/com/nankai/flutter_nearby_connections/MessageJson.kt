package com.nankai.flutter_nearby_connections

import com.google.gson.annotations.SerializedName

data class MessageJson(@SerializedName("deviceID") var deviceID: String,
                       @SerializedName("message") var message: String)