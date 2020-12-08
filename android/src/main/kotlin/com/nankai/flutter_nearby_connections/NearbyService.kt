package com.nankai.flutter_nearby_connections

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context

import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.nearby.connection.*
import io.flutter.plugin.common.MethodChannel

const val NOTIFICATION_ID = 101
const val CHANNEL_ID = "channel"

class NearbyService : Service() {
    private val TAG = "NearbyService"

    private val binder: IBinder = LocalBinder(this)

    private var eventMapping: EventMapping? = null

    private lateinit var serviceType: String

    private lateinit var builder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, getNotification())
        eventMapping = EventMapping(this)
        eventMapping?.create()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationUpdate("$serviceType is destroy!")
        eventMapping?.onDestroy()
    }

    fun initService(channel: MethodChannel, serviceType: String) {
        this@NearbyService.serviceType = serviceType
        eventMapping?.initNearBy(channel, serviceType)
        notificationUpdate("$serviceType is init!")
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun startAdvertising(strategy: Strategy, deviceName: String) {
        Log.d(TAG, "startAdvertising()")
        notificationUpdate("$serviceType is running!")
        eventMapping?.startAdvertising(strategy, deviceName)
    }

    fun startDiscovery(strategy: Strategy) {
        Log.d(TAG, "startDiscovery()")
        notificationUpdate("$serviceType is running!")
        eventMapping?.startDiscovery(strategy)
    }

    fun connect(endpointId: String, displayName: String) {
        eventMapping?.connect(endpointId, displayName)
    }

    fun stopDiscovery() {
        eventMapping?.stopDiscovery()
    }

    fun stopAdvertising() {
        eventMapping?.stopAdvertising()
    }

    fun disconnect(endpointId: String) {
        eventMapping?.disconnect(endpointId)
    }

    fun sendStringPayload(endpointId: String, str: String) {
        eventMapping?.sendStringPayload(endpointId, str)
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