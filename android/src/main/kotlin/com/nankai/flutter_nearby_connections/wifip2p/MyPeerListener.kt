package com.nankai.flutter_nearby_connections.wifip2p

import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.nankai.flutter_nearby_connections.device.DeviceModel

class MyPeerListener(private val wifiP2PEvent: WifiP2PEvent) : WifiP2pManager.PeerListListener {
    val TAG = "MyPeerListener"
    override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
        Log.d(TAG, "OnPeerAvailable()")
        peers?.let { deviceList ->

            wifiP2PEvent.deviceManager.clear()

            if (deviceList.deviceList.isEmpty()) {
                Log.d(TAG, "wifiP2pDeviceList size is zero")
                return
            }
            for (device in deviceList.deviceList) {
                val data = DeviceModel(
                        device.deviceAddress,
                        device.deviceName,
                        getDeviceStatus(device.status)
                )
                wifiP2PEvent.deviceManager.addDevice(data)
                Log.d(TAG, "Found device :" + device.deviceName + " " + device.deviceAddress)
            }
        } ?: Log.d(TAG, "wifiP2pDeviceList is null")
    }
}