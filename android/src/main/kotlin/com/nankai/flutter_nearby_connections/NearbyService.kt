package com.nankai.flutter_nearby_connections

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context

import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.nankai.flutter_nearby_connections.nearbyConnectApi.NearByConnectApiUtils
import com.nankai.flutter_nearby_connections.nearbyConnectApi.notConnected
import com.nankai.flutter_nearby_connections.wifip2p.WifiP2PUtils
import io.flutter.plugin.common.MethodChannel

const val NOTIFICATION_ID = 101
const val CHANNEL_ID = "channel"

class NearbyService : Service() {
    private val TAG = "NearbyService"

    private val binder: IBinder = LocalBinder(this)
    
    private lateinit var nearByConnectApiUtils: NearByConnectApiUtils
    private lateinit var wifiP2pUtils: WifiP2PUtils

    private lateinit var serviceType: String
    private lateinit var connectionsClient: ConnectionsClient


    private lateinit var builder: NotificationCompat.Builder

    private val bluetoothChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> Log.i(TAG, "Bluetooth off")
                    BluetoothAdapter.STATE_TURNING_OFF -> Log.i(TAG, "Turning Bluetooth off...")
                    BluetoothAdapter.STATE_ON -> Log.i(TAG, "Bluetooth on")
                    BluetoothAdapter.STATE_TURNING_ON -> Log.i(TAG, "Turning Bluetooth on...")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, getNotification())
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothChangeReceiver, filter)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothChangeReceiver)
        stopAdvertising()
        stopDiscovery()
        notificationUpdate("$serviceType is destroy!")
        connectionsClient.stopAllEndpoints()
    }

    fun initService(channel: MethodChannel, serviceType: String) {
        this@NearbyService.serviceType = serviceType
        
        this@NearbyService.nearByConnectApiUtils = NearByConnectApiUtils(channel, serviceType,this)
        this@NearbyService.wifiP2pUtils = WifiP2PUtils(channel, serviceType,this)
        
        connectionsClient = Nearby.getConnectionsClient(this)

        notificationUpdate("$serviceType is init!")
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun sendStringPayload(endpointId: String, str: String) {
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(str.toByteArray()))
    }

    fun startAdvertising(strategy: Strategy, deviceName: String) {
        Log.d(TAG, "startAdvertising()")
        notificationUpdate("$serviceType is running!")
        connectionsClient.startAdvertising(
                deviceName, SERVICE_ID, nearByConnectApiUtils.connectionLifecycleCallback,
                AdvertisingOptions.Builder().setStrategy(strategy).build())
    }

    fun startDiscovery(strategy: Strategy) {
        Log.d(TAG, "startDiscovery()")
        notificationUpdate("$serviceType is running!")
        connectionsClient.startDiscovery(
                SERVICE_ID, nearByConnectApiUtils.endpointDiscoveryCallback,
                DiscoveryOptions.Builder().setStrategy(strategy).build())
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    fun disconnect(endpointId: String) {
        connectionsClient.disconnectFromEndpoint(endpointId)
        nearByConnectApiUtils.updateStatus(deviceId = endpointId, state = notConnected)
    }

    fun connect(endpointId: String, displayName: String) {
        connectionsClient.requestConnection(displayName, endpointId, nearByConnectApiUtils.connectionLifecycleCallback)
    }

    private fun getNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID, "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            serviceChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Nearby Service")
                .setContentText("Nearby Service")
                .setSmallIcon(android.R.drawable.stat_notify_sync)

        return builder.build()
    }

    fun notificationUpdate(message: String) {
        builder.setContentText(message)
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}

internal class LocalBinder(private val nearbyService: NearbyService) : Binder() {
    val service: NearbyService
        get() = nearbyService
}