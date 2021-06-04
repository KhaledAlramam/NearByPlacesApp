package com.sedra.nearbyplacesapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*

class LocationInteractor {

    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private var _locationLiveData = MutableLiveData<Location?>()
    val locationLiveData: LiveData<Location?> = _locationLiveData

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                if (locationLiveData.value != null && location != null) {
                    val startLocation = locationLiveData.value
                    val res = startLocation!!.distanceTo(location)
                    if (res > 500) _locationLiveData.postValue(location)
                }
            }
        }
    }
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 30
        fastestInterval = 10
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime = 60
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(context: Context) {
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProvider?.apply {
            lastLocation
                .addOnSuccessListener { location -> // GPS location can be null if GPS is switched off
                    _locationLiveData.postValue(location)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }


    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedLocationProvider?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // stop realtime track

    fun stopLocationUpdates() {
        fusedLocationProvider?.removeLocationUpdates(locationCallback)
    }

    fun onDestroy() {
        stopLocationUpdates()
        fusedLocationProvider = null
    }


}