package com.nankai.flutter_nearby_connections.wifip2p

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import java.util.*

class MyPeerListener : WifiP2pManager.PeerListListener {

    private val peers: List<WifiP2pDevice> = ArrayList()

    override fun onPeersAvailable(wifiP2pDeviceList: WifiP2pDeviceList) {
        val deviceDetails = ArrayList<WifiP2pDevice>()
        Log.d(TAG, "OnPeerAvailable()")
        if (wifiP2pDeviceList != null) {
            if (wifiP2pDeviceList.deviceList.size == 0) {
                Log.d(TAG, "wifiP2pDeviceList size is zero")
                return
            }
            for (device in wifiP2pDeviceList.deviceList) {
                deviceDetails.add(device)
                Log.d(TAG, "Found device :" + device.deviceName + " " + device.deviceAddress)
            }
//            if(mainActivity != null) {
//                mainActivity.setDeviceList(deviceDetails);
//            }
        } else {
            Log.d(TAG, "wifiP2pDeviceList is null")
        }
    }

    companion object {
        const val TAG = "MyPeerListener"
    }
}