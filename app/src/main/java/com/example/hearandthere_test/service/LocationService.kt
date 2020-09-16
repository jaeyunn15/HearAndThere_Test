package com.example.hearandthere_test.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager


import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.hearandthere_test.ui.map.MapActivity
import com.example.hearandthere_test.ui.map.MapsFragment
import com.example.hearandthere_test.util.MapState
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng


open class LocationService : Service() {
    var intent: Intent? = null


    override fun onCreate() {
        super.onCreate()
        intent = Intent(MapState.BR_LOCATION)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        enableLocation()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        MapState.LOCATION_TRACE_ON = false
        disableLocation()
    }

    private fun enableLocation() {
        GoogleApiClient.Builder(applicationContext)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {
                    val locationRequest = LocationRequest().apply {
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = 250
                    }
                    if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    LocationServices.getFusedLocationProviderClient(applicationContext).requestLocationUpdates(locationRequest, locationCallback, null)
                }

                override fun onConnectionSuspended(i: Int) {
                }
            })
            .addApi(LocationServices.API)
            .build()
            .connect()
        MapState.LOCATION_TRACE_ON = true
    }


    private fun disableLocation() {
        LocationServices.getFusedLocationProviderClient(applicationContext).removeLocationUpdates(locationCallback)
        MapState.LOCATION_TRACE_ON = false
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult == null) { return }
            val lastLocation = locationResult.lastLocation
            val coord = LatLng(lastLocation)
            intent?.putExtra("Latitude", coord.latitude)
            intent?.putExtra("Longitude", coord.longitude)
            sendBroadcast(intent)
            MapState.LOCATION_TRACE_ON = true
        }
    }

}