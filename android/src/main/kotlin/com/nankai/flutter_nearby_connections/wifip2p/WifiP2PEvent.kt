package com.nankai.flutter_nearby_connections.wifip2p

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.util.Log
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.Payload
import com.nankai.flutter_nearby_connections.NearbyService
import com.nankai.flutter_nearby_connections.device.DeviceManager
import com.nankai.flutter_nearby_connections.device.DeviceModel
import com.nankai.flutter_nearby_connections.event.NearbyEvent
import com.nankai.flutter_nearby_connections.nearbyConnectApi.CONNECTED
import com.nankai.flutter_nearby_connections.nearbyConnectApi.CONNECTING
import com.nankai.flutter_nearby_connections.nearbyConnectApi.NOT_CONNECTED
import io.flutter.plugin.common.MethodChannel
import java.util.*

class WifiP2PEvent(private val channel: MethodChannel,
                   private val serviceType: String,
                   val deviceManager: DeviceManager,
                   private val service: NearbyService)
    : NearbyEvent,
        WifiP2pManager.DnsSdServiceResponseListener,
        WifiP2pManager.DnsSdTxtRecordListener {

    private val TAG = WifiP2PEvent::class.java.simpleName

    var isWifiP2pEnabled: Boolean = false

    var lastError = -1

    var p2pManager: WifiP2pManager? = null
    var p2pChannel: WifiP2pManager.Channel? = null
    var p2pConfig: WifiP2pConfig = WifiP2pConfig()
    var p2pServiceRequest: WifiP2pDnsSdServiceRequest? = null

    var wifiBroadcastReceiver: WifiBroadcastReceiver? = null
    var wifiManager: WifiManager? = null

    private val r = Random()

    private fun setupIntentFilters(): IntentFilter {
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

    init {

        p2pManager = service.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        p2pChannel = p2pManager?.initialize(
                service.applicationContext,
                service.mainLooper
        ) {
            Log.w(TAG, "Wifi P2P disconnected!")
        }

        wifiManager = service.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager?.wifiState != WifiManager.WIFI_STATE_ENABLED) {
            wifiManager?.isWifiEnabled = true
        }

        // remove the previous list of configured Direct AP networks
        wifiManager?.configuredNetworks?.let {
            for (i in it) {
                if (i.SSID != null) {
                    if (i.SSID.contains("DIRECT")) {
                        wifiManager?.removeNetwork(i.networkId)
                    }
                }
            }
        }

        wifiBroadcastReceiver = WifiBroadcastReceiver(p2pManager, p2pChannel, this@WifiP2PEvent)

        // Wi-Fi Direct Auto Accept authentication (Only PBC supported)
        val wdAutoAccept = WifiDirectAutoAccept(p2pManager, p2pChannel)
        wdAutoAccept.intercept(true)

        // Configure the Intention and WPS in wifi P2P
        p2pConfig.groupOwnerIntent = 8 + r.nextInt(6)
        p2pConfig.wps.setup = WpsInfo.PBC
    }

    fun unregisterReceiver() {
        try {
            service.unregisterReceiver(wifiBroadcastReceiver)
        } catch (e: Exception) {
        }
    }

    /**
     * Mapping events
     */

    override fun startAdvertising(deviceName: String, serviceId: String, build: AdvertisingOptions) {
        val record: MutableMap<String, String> = HashMap()
        record["available"] = "visible"
        val serviceMagnet = WifiP2pDnsSdServiceInfo.newInstance(deviceName, serviceType, record)
        p2pManager?.addLocalService(p2pChannel, serviceMagnet, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                lastError = -1
                Log.i(TAG, "Added local service")
                crateGroup()
            }

            override fun onFailure(reason: Int) {
                lastError = reason
                Log.e(TAG, "Adding local service failed: ${desError(reason)}")
            }
        })
    }

    override fun startDiscovery(serviceId: String, build: DiscoveryOptions) {
        service.registerReceiver(wifiBroadcastReceiver, setupIntentFilters())

        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
		 * by the system when a service is actually discovered.
		 */
        p2pManager?.setDnsSdResponseListeners(p2pChannel, this, this)

        // After attaching listeners, create a service request and initiate discovery.
        p2pServiceRequest = WifiP2pDnsSdServiceRequest.newInstance(serviceType)

        restartServiceDiscovery()
    }

    override fun requestConnection(endpointId: String, displayName: String) {
        p2pConfig.deviceAddress = endpointId
        p2pManager?.connect(p2pChannel, p2pConfig, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                lastError = -1
                Log.i(TAG, "Connect successfully")
            }

            override fun onFailure(reason: Int) {
                lastError = reason
                Log.e(TAG, "Connect failed: ${desError(reason)}")
            }
        })
    }

    override fun stopAdvertising() {
        p2pManager?.clearLocalServices(p2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                lastError = -1
                Log.i(TAG, "Cleared local services")
            }

            override fun onFailure(reason: Int) {
                lastError = reason
                Log.e(TAG, "Clearing local services failed: ${desError(reason)}")
            }
        })

        p2pManager?.removeGroup(p2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.i(TAG, "Group removed successfully")
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "Group remove failed: ${desError(reason)}")
            }
        })
    }

    override fun stopDiscovery() {
        p2pManager?.stopPeerDiscovery(p2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.i(TAG, "Stop peer discovery success")
                p2pManager?.clearServiceRequests(p2pChannel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.i(TAG, "Clear service requests success")
                    }

                    override fun onFailure(reason: Int) {
                        Log.e(TAG, "Clear service requests failed: ${desError(reason)}")
                    }
                })
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "Stop peer discovery failed: ${desError(reason)}")
            }
        })
    }

    override fun stopAllEndpoints() {
        p2pManager?.cancelConnect(p2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.i(TAG, "cancel connect successfully")
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "Current Connection not Terminated: ${desError(reason)}")
            }
        })

        disconnect()
    }

    override fun disconnectFromEndpoint(endpointId: String) {
        p2pManager?.cancelConnect(p2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.i(TAG, "cancel connect successfully")
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "Current Connection not Terminated: ${desError(reason)}")
            }
        })

        p2pManager?.requestGroupInfo(p2pChannel) { group ->
            if (group != null) {
                p2pManager?.removeGroup(p2pChannel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.i(TAG, "Group removed successfully")
                        restartServiceDiscovery()
                        if (group.isGroupOwner) {
                            crateGroup()
                        }
                    }

                    override fun onFailure(reason: Int) {
                        Log.d(TAG, "removeGroup onFailure: ${desError(reason)}")
                    }
                })
            }
        }
    }

    override fun sendPayload(endpointId: String, fromBytes: Payload) {
    }

    override fun onDispose() {
        unregisterReceiver()
        stopAdvertising()
        stopDiscovery()
        stopAllEndpoints()
    }

    override fun onDnsSdServiceAvailable(instanceName: String?, serviceType: String?, device: WifiP2pDevice?) {
        Log.i(TAG, "Found Service, :$instanceName, type$serviceType:")
        if (serviceType?.startsWith(this@WifiP2PEvent.serviceType) == true) {
            device?.let {
                val data = DeviceModel(it.deviceAddress,
                        it.deviceName,
                        getDeviceStatus(it.status)
                )
                deviceManager.addDevice(data)
            }
        } else {
            Log.w(TAG, "Not our Service, :${this@WifiP2PEvent.serviceType}!=$serviceType:")
        }
    }

    override fun onDnsSdTxtRecordAvailable(fullDomainName: String?, txtRecordMap: MutableMap<String, String>?, srcDevice: WifiP2pDevice?) {

    }

    private fun restartServiceDiscovery() {
        p2pManager?.stopPeerDiscovery(p2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.i(TAG, "Stop peer discovery success")
                p2pManager?.addServiceRequest(p2pChannel, p2pServiceRequest, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.i(TAG, "Added service discovery request")
                        p2pManager?.discoverServices(p2pChannel, object : WifiP2pManager.ActionListener {
                            override fun onSuccess() {
                                Log.i(TAG, "Service discovery initiated")
                            }

                            override fun onFailure(reason: Int) {
                                Log.e(TAG, "Service discovery failed: ${desError(reason)}")
                            }
                        })
                    }

                    override fun onFailure(reason: Int) {
                        Log.e(TAG, "Failed adding service discovery request: ${desError(reason)}")
                    }
                })
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "Stop peer discovery failed: ${desError(reason)}")
            }
        })
    }

    private fun disconnect() {
        p2pManager?.requestGroupInfo(p2pChannel) { group ->
            if (group != null) {
                p2pManager?.removeGroup(p2pChannel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.i(TAG, "Group removed successfully")
                    }

                    override fun onFailure(reason: Int) {
                        Log.d(TAG, "removeGroup onFailure: ${desError(reason)}")
                    }
                })
            }
        }
    }

    private fun crateGroup() {
        p2pManager?.createGroup(p2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.i(TAG, "Group created successfully")
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "Group create failed: ${desError(reason)}")
            }
        })
    }

}

fun desError(errorCode: Int): String {
    return when (errorCode) {
        0 -> "internal error"
        1 -> "p2p unsupported"
        2 -> "framework busy"
        3 -> "no service requests"
        else -> "Unknown error!"
    }
}

fun getDeviceStatus(statusCode: Int): Int {
    return when (statusCode) {
        WifiP2pDevice.CONNECTED -> CONNECTED
        WifiP2pDevice.INVITED -> CONNECTING
        WifiP2pDevice.FAILED -> NOT_CONNECTED
        WifiP2pDevice.AVAILABLE -> NOT_CONNECTED
        WifiP2pDevice.UNAVAILABLE -> NOT_CONNECTED
        else -> NOT_CONNECTED
    }
}