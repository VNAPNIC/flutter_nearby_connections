package com.nankai.flutter_nearby_connections.wifip2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

class WifiBroadcastReceiver(private val wifiP2pManager: WifiP2pManager?,
                            private val channel: WifiP2pManager.Channel?) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action) {
            val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "WIFI P2P ENABLED")
            } else {
                Log.d(TAG, "WIFI P2P NOT ENABLED")
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action) {
            Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION")
            if (wifiP2pManager != null) {
                val myPeerListener = MyPeerListener()
                wifiP2pManager.requestPeers(channel, myPeerListener)
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action) {
            if (wifiP2pManager == null) {
                return
            }
            val networkInfo = intent
                    .getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
            //WifiP2pInfo p2pInfo = intent
            //        .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);

            //if (p2pInfo != null && p2pInfo.groupOwnerAddress != null) {
            //    String goAddress = Utils.getDottedDecimalIP(p2pInfo.groupOwnerAddress
            //            .getAddress());
            //    boolean isGroupOwner = p2pInfo.isGroupOwner;
            //     Log.d(WifiBroadcastReceiver.TAG,"I am a group owner");
            // }
            if (networkInfo.isConnected) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                //mManager.requestConnectionInfo(mChannel, mActivity);
            } else {
                // It's a disconnect
                Log.d(TAG, "Its a disconnect")

                //activity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {
            Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
            // Respond to this device's wifi state changing
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION == action) {
            val state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, 10000)
            if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                //TODO
            } else if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                //TODO
            }
        }
    }

    companion object {
        const val TAG = "===WifiBReceiver"
    }
}