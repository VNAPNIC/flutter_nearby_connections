package com.nankai.flutter_nearby_connections.wifip2p

import android.net.wifi.p2p.WifiP2pManager
import io.flutter.plugin.common.MethodChannel

class ResultActionListener(private val result: MethodChannel.Result) : WifiP2pManager.ActionListener {
    override fun onSuccess() {
        result.success(true)
    }

    override fun onFailure(reasonCode: Int) {
        result.success(false)
    }
}