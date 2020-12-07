package com.nankai.flutter_nearby_connections.wifip2p

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.nankai.flutter_nearby_connections.NearbyService
import io.flutter.plugin.common.MethodChannel

class WifiP2PUtils(private val channel: MethodChannel, private val serviceType: String, private val service: NearbyService) : WifiP2pManager.ConnectionInfoListener {
    private val TAG = "WifiP2pUtils"

    var wifiP2pManager: WifiP2pManager? = null
    var wifiP2pChannel: WifiP2pManager.Channel? = null
    var wifiBroadcastReceiver: WifiBroadcastReceiver? = null
    var hostAddress: String? = null

    private fun setupIntentFilters() : IntentFilter{
        val intentFilter = IntentFilter()
         intentFilter.apply {
            // Indicates a change in the Wi-Fi P2P status.
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            // Indicates a change in the list of available peers.
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            // Indicates the state of Wi-Fi P2P connectivity has changed.
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            // Indicates this device'base details have changed.
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            // Indicates the state of peer discovery has changed
            addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
        }

        return intentFilter
    }

    private fun initWifiP2P() {
        wifiP2pManager = service.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        wifiP2pChannel = wifiP2pManager?.initialize(
                service.applicationContext,
                service.mainLooper
        ) { TODO("Not yet implemented") }

        wifiP2pManager?.let { wifiP2pManager ->
            wifiP2pChannel?.let { wifiP2pChannel ->
                wifiBroadcastReceiver = WifiBroadcastReceiver(wifiP2pManager, wifiP2pChannel)
                service.registerReceiver(wifiBroadcastReceiver, setupIntentFilters())
            }
        }
    }

    override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo?) {
        this.hostAddress = wifiP2pInfo?.groupOwnerAddress?.hostAddress
        Log.d(TAG, "wifiP2pInfo groupOwnerAddress getHostAddress() $hostAddress")
    }
}