package com.nankai.flutter_nearby_connections.wifip2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

class WifiBroadcastReceiver(private val p2pManager: WifiP2pManager?,
                            private val channel: WifiP2pManager.Channel?,
                            private val wifiP2PEvent: WifiP2PEvent) : BroadcastReceiver(),
        WifiP2pManager.ConnectionInfoListener {

    private val TAG = "WifiBroadcastReceiver"

    private var thisDevice: WifiP2pDevice? = null
    private var wifiP2pGroup: WifiP2pGroup? = null
    private var isGroupOwner = false

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

    /**
     * Available extras: [WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION]
     */
    private fun onStateChanged(intent: Intent) {
        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
        val isConnected = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
        wifiP2PEvent.isWifiP2pEnabled = isConnected
        Log.i(TAG, "WIFI P2P ${if (isConnected) "NOT ENABLED" else "ENABLED"}")
    }

    /**
     * Available extras: [WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION]
     */
    private fun onPeersChanged() {
        Log.i(TAG, "【Peers Changed】")
        val myPeerListener = MyPeerListener(wifiP2PEvent)
        p2pManager?.requestPeers(channel, myPeerListener)
    }

    /**
     * Available extras: [WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION]
     */
    private fun onConnectionChanged(intent: Intent) {
        val networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo

        if (networkInfo.isConnected) {
            Log.i(TAG, "Connected to P2P network. Requesting connection info")
            p2pManager?.requestConnectionInfo(channel, this@WifiBroadcastReceiver)
        } else {
            Log.e(TAG, "Wi-Fi Direct Disconnected.")
        }
    }

    /**
     * Available extras: [WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION]
     */
    private fun onThisDeviceChanged(intent: Intent) {
        thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE) as WifiP2pDevice
        Log.i(TAG, "\n【This Device Changed】\n" +
                "deviceName: ${thisDevice?.deviceName}\n" +
                "deviceAddress: ${thisDevice?.deviceAddress}\n" +
                "status: ${thisDevice?.status}\n" +
                "primaryDeviceType: ${thisDevice?.primaryDeviceType}\n" +
                "secondaryDeviceType: ${thisDevice?.secondaryDeviceType}\n" +
                "isGroupOwner: ${thisDevice?.isGroupOwner}")
    }

    /**
     * Available extras: [WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION]
     */
    private fun onDiscoveryChanged(intent: Intent) {
        val discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED)
        Log.i(TAG, "【Discovery Changed】 State:${discoveryState}")
    }
    
    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        this.isGroupOwner = info?.isGroupOwner == true

        // If we're the server
        if (isGroupOwner) {
            p2pManager?.requestGroupInfo(channel) { wifiP2pGroup ->
                if (wifiP2pGroup != null) {
                    Log.i(TAG, "\n【Group Info】\n${p2pGroupToString(wifiP2pGroup)}")
                    this@WifiBroadcastReceiver.wifiP2pGroup = wifiP2pGroup
                }
            }
        }
    }

    private fun p2pGroupToString(wifiP2pGroup: WifiP2pGroup?): String {
        return if (wifiP2pGroup != null) {
            var strWifiP2pGroup = "Network name: " + wifiP2pGroup.networkName
            strWifiP2pGroup += """
                 
                 Is group owner: ${wifiP2pGroup.isGroupOwner}
                 """.trimIndent()
            if (wifiP2pGroup.owner != null) {
                strWifiP2pGroup += "\nGroup owner: "
                strWifiP2pGroup += """
                     
                     ${p2pDeviceToString(wifiP2pGroup.owner)}
                     """.trimIndent()
            }
            if (wifiP2pGroup.clientList != null && !wifiP2pGroup.clientList.isEmpty()) {
                for (client in wifiP2pGroup.clientList) {
                    strWifiP2pGroup += "\nClient: "
                    strWifiP2pGroup += """
                         
                         ${p2pDeviceToString(client)}
                         """.trimIndent()
                }
            }
            strWifiP2pGroup
        } else {
            Log.e(TAG, "WifiP2pGroup is null")
            ""
        }
    }

    /**
     * Takes a WifiP2pDevice and returns a String of readable device information
     * @param wifiP2pDevice
     * @return
     */
    private fun p2pDeviceToString(wifiP2pDevice: WifiP2pDevice?): String {
        return if (wifiP2pDevice != null) {
            var strDevice = "\nDevice name: " + wifiP2pDevice.deviceName
            strDevice += """
                  
                  \nDevice address: ${wifiP2pDevice.deviceAddress}
                  """.trimIndent()
            strDevice += if (wifiP2pDevice == thisDevice) {
                """
     
     Is group owner: $isGroupOwner
     """.trimIndent()
            } else {
                "\nIs group owner: false"
            }
            strDevice += """
                  
                  Status: ${deviceStatusToString(wifiP2pDevice.status).toString()}
                  
                  """.trimIndent()
            strDevice
        } else {
            Log.e(TAG, "WifiP2pDevice is null")
            ""
        }
    }
}

/**
 * Translates a device status code to a readable String status
 * @param status
 * @return A readable String device status
 */
fun deviceStatusToString(status: Int): String {
    return when (status) {
        WifiP2pDevice.AVAILABLE -> {
            "Available"
        }
        WifiP2pDevice.INVITED -> {
            "Invited"
        }
        WifiP2pDevice.CONNECTED -> {
            "Connected"
        }
        WifiP2pDevice.FAILED -> {
            "Failed"
        }
        WifiP2pDevice.UNAVAILABLE -> {
            "Unavailable"
        }
        else -> {
            "Unknown"
        }
    }
}