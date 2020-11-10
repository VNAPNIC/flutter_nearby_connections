package com.nankai.flutter_nearby_connections

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == LOCATION_ENABLE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
               //TODO something
            } else {
                //TODO something
            }
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray): Boolean {
        if (requestCode == REQUEST_LOCATION_PERMISSION && permissions.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initiateLocationServiceRequest()
                requestLocationEnable()
            }
            return true
        }
        return false
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
        task.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)
            } catch (ex: ApiException) {
                when (ex.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {}
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = ex as ResolvableApiException
                        resolvableApiException
                                .startResolutionForResult(activity, LOCATION_ENABLE_REQUEST)
                    } catch (e: IntentSender.SendIntentException) {
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun requestLocationPermission(result: Result?) {
        if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initiateLocationServiceRequest()
            requestLocationEnable()
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }
}