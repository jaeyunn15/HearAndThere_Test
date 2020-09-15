package com.example.hearandthere_test.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager


import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.hearandthere_test.MyApplication.Companion.context
import com.example.hearandthere_test.util.MapState


open class LocationService : Service() {
    private var locationManager: LocationManager? = null
    private var listener: MyLocationListener? = null
    var intent: Intent? = null

    override fun onCreate() {
        super.onCreate()
        intent = Intent(MapState.BR_LOCATION)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        listener = MyLocationListener()
        startLocationTrace()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("STOP_SERVICE", "DONE")
        MapState.LOCATION_TRACE_ON = false
        stopLocationTrace()
    }

    fun startLocationTrace(){
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return
        }
        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            3,
            MapState.MIN_DISTANCE,
            listener as LocationListener
        )
//        locationManager?.requestLocationUpdates(
//            LocationManager.GPS_PROVIDER,
//            3,
//            MapState.MIN_DISTANCE,
//            listener as LocationListener
//        )
        MapState.LOCATION_TRACE_ON = true
    }

    fun stopLocationTrace(){
        locationManager?.removeUpdates(listener as LocationListener)
        MapState.LOCATION_TRACE_ON = false
    }

    inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(loc: Location) {
            intent?.putExtra("Latitude", loc.latitude)
            intent?.putExtra("Longitude", loc.longitude)
            Log.d("오디오 테스트 위치추적", "${loc.latitude} || ${loc.longitude}")
            sendBroadcast(intent)
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderDisabled(provider: String?) {
            Toast.makeText(applicationContext, "Gps Disabled", Toast.LENGTH_SHORT).show()
            MapState.LOCATION_TRACE_ON = false
        }

        override fun onProviderEnabled(provider: String?) {
            Toast.makeText(applicationContext, "Gps Enabled", Toast.LENGTH_SHORT).show()
            MapState.LOCATION_TRACE_ON = true
        }
    }
}