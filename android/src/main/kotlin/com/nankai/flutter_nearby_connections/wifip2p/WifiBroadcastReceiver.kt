package com.nankai.flutter_nearby_connections.wifip2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

class WifiBroadcastReceiver(private val p2pManager: WifiP2pManager?,
                            private val channel: WifiP2pManager.Channel?,
                            private val wifiP2PEvent: WifiP2PEvent) : BroadcastReceiver() {

    private val TAG = "WifiBroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) {
            return
        }

        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> onStateChanged(intent)
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> onPeersChanged()
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> onConnectionChanged(intent)
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> onThisDeviceChanged(intent)
            WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION -> onDiscoveryChanged(intent)
        }
    }

    private fun onStateChanged(intent: Intent) {
        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
        val isConnected = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
        wifiP2PEvent.isWifiP2pEnabled = isConnected
        Log.d(TAG, "WIFI P2P ${if (isConnected) "NOT ENABLED" else "ENABLED"}")
    }

    private fun onPeersChanged() {
        Log.i(TAG, "onPeersChanged")
        val myPeerListener = MyPeerListener(wifiP2PEvent)
        p2pManager?.requestPeers(channel, myPeerListener)
    }

    private fun onConnectionChanged(intent: Intent) {
        val p2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO) as WifiP2pInfo
        val networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo

        Log.i(TAG, "onConnectionChanged p2pInfo:\n" +
                "groupOwnerAddress: ${p2pInfo.groupOwnerAddress}\n" +
                "isGroupOwner: ${p2pInfo.isGroupOwner}\n" +
                "groupFormed: ${p2pInfo.groupFormed}")
        Log.i(TAG, "onConnectionChanged networkInfo:\n" +
                "subtypeName: ${networkInfo.subtypeName}\n" +
                "isConnected: ${networkInfo.isConnected}")
    }

    private fun onThisDeviceChanged(intent: Intent) {
        val device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE) as WifiP2pDevice
        Log.i(TAG, "onThisDeviceChanged device:\n" +
                "deviceName: ${device.deviceName}\n" +
                "deviceAddress: ${device.deviceAddress}\n" +
                "status: ${device.status}\n" +
                "primaryDeviceType: ${device.primaryDeviceType}\n" +
                "secondaryDeviceType: ${device.secondaryDeviceType}\n" +
                "isGroupOwner: ${device.isGroupOwner}")
    }

    private fun onDiscoveryChanged(intent: Intent) {
        val discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED)
        Log.i(TAG, "onDiscoveryChanged discoveryState:${discoveryState}")
    }
}