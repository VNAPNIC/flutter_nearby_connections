package com.nankai.flutter_nearby_connections

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startForegroundService
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import kotlin.system.exitProcess


const val SERVICE_ID = "flutter_nearby_connections"

const val initNearbyService = "init_nearby_service"
const val startAdvertisingPeer = "start_advertising_peer"
const val startBrowsingForPeers = "start_browsing_for_peers"

const val stopAdvertisingPeer = "stop_advertising_peer"
const val stopBrowsingForPeers = "stop_browsing_for_peers"

const val invitePeer = "invite_peer"
const val disconnectPeer = "disconnect_peer"

const val sendMessage = "send_message"

const val INVOKE_CHANGE_STATE_METHOD = "invoke_change_state_method"
const val INVOKE_MESSAGE_RECEIVE_METHOD = "invoke_message_receive_method"

const val NEARBY_RUNNING = "nearby_running"

/** FlutterNearbyConnectionsPlugin */
class FlutterNearbyConnectionsPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var locationHelper: LocationHelper? = null
    private lateinit var activity: Activity
    private var binding: ActivityPluginBinding? = null
    private lateinit var callbackUtils: CallbackUtils
    private var mService: NearbyService? = null

    private lateinit var localDeviceName: String
    private lateinit var strategy: Strategy
    private lateinit var connectionsClient: ConnectionsClient
    private var mBound = false

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, viewTypeId)
        channel.setMethodCallHandler(this)
    }

    companion object {
        private const val viewTypeId = "flutter_nearby_connections"

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), viewTypeId)
            channel.setMethodCallHandler(FlutterNearbyConnectionsPlugin())
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            initNearbyService -> {
                connectionsClient = Nearby.getConnectionsClient(activity)
                callbackUtils = CallbackUtils(channel, activity)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(activity, Intent(activity, NearbyService::class.java))
                }

                val intent = Intent(activity, NearbyService::class.java)
                activity.bindService(intent, connection, Context.BIND_AUTO_CREATE)

                localDeviceName = if (call.argument<String>("deviceName").isNullOrEmpty())
                    Build.MANUFACTURER + " " + Build.MODEL
                else
                    call.argument<String>("deviceName")!!

                strategy = when (call.argument<Int>("strategy")) {
                    0 -> Strategy.P2P_CLUSTER
                    1 -> Strategy.P2P_STAR
                    else -> Strategy.P2P_POINT_TO_POINT
                }

                locationHelper?.requestLocationPermission(result)
            }
            startAdvertisingPeer -> {
                Log.d("nearby_connections", "startAdvertisingPeer")
                mService?.startAdvertising(strategy, localDeviceName)
            }
            startBrowsingForPeers -> {
                Log.d("nearby_connections", "startBrowsingForPeers")
                mService?.startDiscovery(strategy)
            }
            stopAdvertisingPeer -> {
                Log.d("nearby_connections", "stopAdvertisingPeer")
                mService?.stopAdvertising()
                result.success(true)
            }
            stopBrowsingForPeers -> {
                Log.d("nearby_connections", "stopDiscovery")
                mService?.stopDiscovery()
                result.success(true)
            }
            invitePeer -> {
                Log.d("nearby_connections", "invitePeer")
                val deviceId = call.argument<String>("deviceId")
                val displayName = call.argument<String>("deviceName")
                mService?.connect(deviceId!!, displayName!!)
            }
            disconnectPeer -> {
                Log.d("nearby_connections", "disconnectPeer")
                val deviceId = call.argument<String>("deviceId")
                mService?.disconnect(deviceId!!)
                callbackUtils.updateStatus(deviceId = deviceId!!, state = notConnected)
                result.success(true)
            }
            sendMessage -> {
                Log.d("nearby_connections", "sendMessage")
                val deviceId = call.argument<String>("deviceId")
                val message = call.argument<String>("message")
                mService?.sendStringPayload(deviceId!!, message!!)
            }
        }
    }

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as LocalBinder
            mService = binder.service
            mService?.initService(callbackUtils)
            mBound = true
            channel.invokeMethod(NEARBY_RUNNING, mBound)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
            channel.invokeMethod(NEARBY_RUNNING, mBound)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        mService?.onDestroy()
        locationHelper = null
        exitProcess(0)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.binding = binding
        activity = binding.activity
        locationHelper = LocationHelper(binding.activity)
        locationHelper?.let {
            binding.addActivityResultListener(it)
            binding.addRequestPermissionsResultListener(it)
        }
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivity() {
        locationHelper?.let {
            binding?.removeRequestPermissionsResultListener(it)
            binding?.removeActivityResultListener(it)
        }
        binding = null
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }
}
