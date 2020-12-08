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
import com.nankai.flutter_nearby_connections.event.EventMapping
import com.nankai.flutter_nearby_connections.event.NearbyServiceEvent
import io.flutter.plugin.common.MethodChannel

const val NOTIFICATION_ID = 101
const val CHANNEL_ID = "channel"

class NearbyService : Service() , NearbyServiceEvent {
    private val TAG = "NearbyService"

    private val binder: IBinder = LocalBinder(this)

    private var mapping: EventMapping? = null

    private lateinit var serviceType: String

    private lateinit var builder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, getNotification())
        mapping = EventMapping(this)
        mapping?.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStart(channel: MethodChannel, serviceType: String) {
        this@NearbyService.serviceType = serviceType

        notificationUpdate("$serviceType is start!")

        mapping?.onStart(channel, serviceType)
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationUpdate("$serviceType is destroy!")
        mapping?.onDestroy()
    }

    override fun startAdvertising(strategy: Strategy, deviceName: String) {
        Log.d(TAG, "startAdvertising()")
        notificationUpdate("$serviceType is running!")
        mapping?.startAdvertising(strategy, deviceName)
    }

    override fun startDiscovery(strategy: Strategy, deviceName: String) {
        Log.d(TAG, "startDiscovery()")
        notificationUpdate("$serviceType is running!")
        mapping?.startDiscovery(strategy, deviceName)
    }

    override fun connect(endpointId: String, displayName: String) {
        mapping?.connect(endpointId, displayName)
    }

    override fun stopDiscovery() {
        mapping?.stopDiscovery()
    }

    override fun stopAdvertising() {
        mapping?.stopAdvertising()
    }

    override fun disconnect(endpointId: String) {
        mapping?.disconnect(endpointId)
    }

    override fun sendStringPayload(endpointId: String, str: String) {
        mapping?.sendStringPayload(endpointId, str)
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