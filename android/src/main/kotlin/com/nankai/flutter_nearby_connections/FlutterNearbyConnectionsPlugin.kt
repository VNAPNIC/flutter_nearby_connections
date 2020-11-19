package com.nankai.flutter_nearby_connections

import android.app.Activity
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

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

/** FlutterNearbyConnectionsPlugin */
class FlutterNearbyConnectionsPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var locationHelper: LocationHelper? = null
    private lateinit var activity: Activity
    private var binding: ActivityPluginBinding? = null
    private lateinit var callbackUtils: CallbackUtils

    private lateinit var localDeviceId: String
    private lateinit var localDeviceName: String
    private lateinit var strategy: Strategy

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
                callbackUtils = CallbackUtils(channel, activity)
                localDeviceId = call.argument<String>("deviceId")!!
                localDeviceName = if (call.argument<String>("deviceName").isNullOrEmpty())
                    android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
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
                val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
                Nearby.getConnectionsClient(activity).startAdvertising(localDeviceName, SERVICE_ID,
                        callbackUtils.connectionLifecycleCallback, advertisingOptions)
                        .addOnSuccessListener {
                            Log.d("nearby_connections", "startAdvertising")
                            result.success(true)
                        }.addOnFailureListener { e -> result.error("Failure", e.message, null) }
            }
            startBrowsingForPeers -> {
                Log.d("nearby_connections", "startBrowsingForPeers")
                val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
                Nearby.getConnectionsClient(activity)
                        .startDiscovery(SERVICE_ID, callbackUtils.endpointDiscoveryCallback, discoveryOptions)
                        .addOnSuccessListener {
                            Log.d("nearby_connections", "startDiscovery")
                            result.success(true)
                        }.addOnFailureListener { e -> result.error("Failure", e.message, null) }
            }
            stopAdvertisingPeer -> {
                Log.d("nearby_connections", "stopAdvertisingPeer")
                Nearby.getConnectionsClient(activity).stopAdvertising()
                result.success(true)
            }
            stopBrowsingForPeers -> {
                Log.d("nearby_connections", "stopDiscovery")
                Nearby.getConnectionsClient(activity).stopDiscovery()
                result.success(true)
            }
            invitePeer -> {
                Log.d("nearby_connections", "invitePeer")
                val deviceId = call.argument<String>("deviceId")
                val displayName = call.argument<String>("deviceName")
                Nearby.getConnectionsClient(activity)
                        .requestConnection(displayName!!, deviceId!!, callbackUtils.connectionLifecycleCallback)
                        .addOnSuccessListener { result.success(true) }
                        .addOnFailureListener { e -> result.error("Failure", e.message, null) }
            }
            disconnectPeer -> {
                Log.d("nearby_connections", "disconnectPeer")
                val deviceId = call.argument<String>("deviceId")
                Nearby.getConnectionsClient(activity).disconnectFromEndpoint(deviceId!!)
                callbackUtils.updateStatus(deviceId = deviceId, state = notConnected)
                result.success(true)
            }
            sendMessage -> {
                Log.d("nearby_connections", "sendMessage")
                val deviceId = call.argument<String>("deviceId")
                val message = call.argument<String>("message")
                Nearby.getConnectionsClient(activity).sendPayload(deviceId!!, Payload.fromBytes(message!!.toByteArray())).addOnCompleteListener {
                    result.success(true)
                }
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        locationHelper = null
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
