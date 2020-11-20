package com.nankai.flutter_nearby_connections

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

const val LOCATION_ENABLE_REQUEST = 777
const val REQUEST_LOCATION_PERMISSION = 7777

class LocationHelper(private val activity: Activity) : PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {

    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var result: Result? = null

    private fun requestLocationPermission() {
        requestLocationPermission(result!!)
    }

    fun requestLocationPermission(result: Result) {
        this.result = result
        ActivityCompat.requestPermissions(activity, arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
            if (requestCode == LOCATION_ENABLE_REQUEST) {
                result = if (resultCode == Activity.RESULT_OK) {
//                    initiateLocationServiceRequest()
//                    requestLocationEnable()
                    result?.success(true)
                    null
                } else {
                    result?.success(false)
                    requestLocationPermission()
                    null
                }
                true
            } else {
                result?.success(false)
                false
            }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray): Boolean =
            if (requestCode == REQUEST_LOCATION_PERMISSION && permissions.isNotEmpty()) {
                result = if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    initiateLocationServiceRequest()
//                    requestLocationEnable()
                    result?.success(true)
                    null
                } else {
                    result?.success(false)
                    null
                }
                true
            } else {
                false
            }

    private fun initiateLocationServiceRequest() {
        val mLocationRequest = LocationRequest.create()
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .setAlwaysShow(true)
        mLocationSettingsRequest = builder.build()
    }

    private fun requestLocationEnable() {
        val task = LocationServices.getSettingsClient(activity)
                .checkLocationSettings(mLocationSettingsRequest)
        task.addOnCompleteListener { t ->
            try {
                t.getResult(ApiException::class.java)
                result?.success(true)
            } catch (ex: ApiException) {
                when (ex.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {
                        result?.success(true)
                    }
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = ex as ResolvableApiException
                        resolvableApiException
                                .startResolutionForResult(activity, LOCATION_ENABLE_REQUEST)
                    } catch (e: IntentSender.SendIntentException) {
                        result?.error("LOCATION_SERVICE_ERROR", e.message, null)
                    }
                    else -> {
                        result?.success(false)
                    }
                }
            }
        }
    }
}