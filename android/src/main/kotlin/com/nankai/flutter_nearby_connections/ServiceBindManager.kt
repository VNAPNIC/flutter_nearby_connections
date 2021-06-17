package com.nankai.flutter_nearby_connections

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.atomic.AtomicBoolean

class ServiceBindManager(
    val context: Context,
    val channel: MethodChannel,
    val callback: CallbackUtils
) {

    val TAG: String = "ServiceBindManager"
    var mService: NearbyService? = null

    private val isBound: AtomicBoolean = AtomicBoolean(false)

    private var intent: Intent = Intent(context, NearbyService::class.java)

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected: $context")
            val binder = service as LocalBinder
            mService = binder.service
            isBound.set(true)
            mService?.initService(callback)
            channel.invokeMethod(NEARBY_RUNNING, isBound.get())
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected: $context")
            isBound.set(false)
            channel.invokeMethod(NEARBY_RUNNING, isBound.get())
        }

        override fun onBindingDied(name: ComponentName?) {
            Log.d(TAG, "onBindingDied: $context")
            isBound.set(false)
            channel.invokeMethod(NEARBY_RUNNING, isBound.get())
        }

        override fun onNullBinding(name: ComponentName?) {
            Log.d(TAG, "onNullBinding: $context")
            isBound.set(false)
            channel.invokeMethod(NEARBY_RUNNING, isBound.get())
        }
    }


    fun bindService() {
        Log.e(TAG, "bindService: $context")
        isBound.set(context.bindService(intent, connection, BIND_AUTO_CREATE))
    }

    fun unbindService() {
        try {
            Log.e(TAG, "unbindService: $context")
            if (isBound.get()) {
                isBound.set(false)
                context.unbindService(connection)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mService?.stopForeground(true)
        mService?.stopSelf()
    }

}
