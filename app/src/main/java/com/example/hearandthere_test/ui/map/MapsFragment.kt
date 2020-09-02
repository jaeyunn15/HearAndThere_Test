package com.example.hearandthere_test.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.os.Trace
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.hearandthere_test.R
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.MapFragment
import kotlinx.android.synthetic.main.fragment_maps.*

class MapsFragment : Fragment(), OnMapReadyCallback {
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult == null) {
                return
            }
            val lastLocation = locationResult.lastLocation
            val coord = LatLng(lastLocation)
            val locationOverlay = map.locationOverlay
            locationOverlay.position = coord
            locationOverlay.bearing = lastLocation.bearing
            Log.d("현재 위치:" ,"${coord.latitude} || ${coord.longitude}")
            map.moveCamera(CameraUpdate.scrollTo(coord))
            if (waiting) {
                waiting = false
                fab.setImageResource(R.drawable.ic_baseline_location_disabled_24)
                locationOverlay.isVisible = true
            }
        }
    }

    private var trackingEnabled: Boolean = false
    private var locationEnabled: Boolean = false
    private var waiting: Boolean = false
    private lateinit var map: NaverMap
    private lateinit var fab : FloatingActionButton


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View =  inflater.inflate(R.layout.fragment_maps, container, false)
        fab = view.findViewById(R.id.fab)

        val mapFragment = requireFragmentManager().findFragmentById(R.id.maps_frag_view) as MapFragment?
            ?: MapFragment.newInstance().also {
                requireFragmentManager().beginTransaction().add(R.id.maps_frag_view, it).commit()
            }
        mapFragment.getMapAsync(this)

        tryEnableLocation()

        return view
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PermissionChecker.PERMISSION_GRANTED }) {
                enableLocation()
            } else {
                fab.setImageResource(R.drawable.ic_baseline_my_location_24)
            }
            return
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        if (trackingEnabled) {
            enableLocation()
        }
    }

    override fun onStop() {
        super.onStop()
        disableLocation()
    }

    override fun onMapReady(naverMap: NaverMap) {
        map = naverMap

        fab.setOnClickListener {
            if (trackingEnabled){
                disableLocation()
                fab.setImageResource(R.drawable.ic_baseline_my_location_24)
            }else{
                fab.setImageDrawable(CircularProgressDrawable(requireContext()).apply {
                    setStyle(CircularProgressDrawable.LARGE)
                    setColorSchemeColors(Color.WHITE)
                    start()
                })
                tryEnableLocation()
            }
            trackingEnabled = !trackingEnabled
        }
    }

    private fun tryEnableLocation() {
        if (PERMISSIONS.all { ContextCompat.checkSelfPermission(requireContext(), it) == PermissionChecker.PERMISSION_GRANTED }) {
            enableLocation()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_REQUEST_CODE)
        }
    }

    private fun enableLocation() {
        GoogleApiClient.Builder(requireContext())
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                @SuppressLint("MissingPermission")
                override fun onConnected(bundle: Bundle?) {
                    val locationRequest = LocationRequest().apply {
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = LOCATION_REQUEST_INTERVAL.toLong()
                        fastestInterval = LOCATION_REQUEST_INTERVAL.toLong()
                    }

                    LocationServices.getFusedLocationProviderClient(requireContext())
                        .requestLocationUpdates(locationRequest, locationCallback, null)
                    locationEnabled = true
                    waiting = true

                }
                override fun onConnectionSuspended(i: Int) {
                }
            })
            .addApi(LocationServices.API)
            .build()
            .connect()
        updateFAB()
    }

    private fun disableLocation() {
        if (!locationEnabled) {
            return
        }
        LocationServices.getFusedLocationProviderClient(requireContext()).removeLocationUpdates(locationCallback)
        locationEnabled = false
        updateFAB()
    }

    private fun updateFAB(){
        if (trackingEnabled){
            fab.setImageResource(R.drawable.ic_baseline_my_location_24)
        }else{
            fab.setImageResource(R.drawable.ic_baseline_location_disabled_24)
        }
    }

    companion object {
        private const val LOCATION_REQUEST_INTERVAL = 500 //10초마다 알려줌
        private const val PERMISSION_REQUEST_CODE = 100
        private val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
    }

}