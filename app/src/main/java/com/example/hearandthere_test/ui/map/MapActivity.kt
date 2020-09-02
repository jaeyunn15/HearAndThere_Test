package com.example.hearandthere_test.ui.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.hearandthere_test.R
import com.google.android.gms.common.internal.Preconditions
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource


class MapActivity : AppCompatActivity(){
    private lateinit var locationSource : FusedLocationSource
    private lateinit var naverMap: NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var fragment = supportFragmentManager.findFragmentById(R.id.container)

        var mapFragment = MapFragment()
        var mapsFragment = MapsFragment()

        if (fragment == null){
            fragment = mapsFragment
            addFragmentToActivity(
                supportFragmentManager,
                fragment,
                R.id.container
            )
        }

//        val mapView = findViewById<MapView>(R.id.map_view)
//        mapView.onCreate(savedInstanceState)
//
//        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
//
//        mapView.getMapAsync {
//
//            it.locationSource = FusedLocationSource(
//                this,
//                LOCATION_PERMISSION_REQUEST_CODE
//            )
//            //it.locationTrackingMode = LocationTrackingMode.Follow
//            it.uiSettings.isLocationButtonEnabled = true
//            naverMap = it
//            it.addOnCameraChangeListener { i, b ->
//                val cameraPosition : CameraPosition = it.cameraPosition
//                Log.d("변경된 위경도 : ","${cameraPosition.target.latitude}  || ${cameraPosition.target.longitude}")
//            }
//        }

    }

    fun addFragmentToActivity(fragmentManager: FragmentManager, fragment: Fragment, frameId : Int){
        Preconditions.checkNotNull(fragmentManager)
        Preconditions.checkNotNull(fragment)
        val transaction = fragmentManager.beginTransaction()
        transaction.add(frameId, fragment)
        transaction.commit()
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        if (locationSource.onRequestPermissionsResult(requestCode,permissions, grantResults)){
//            if (!locationSource.isActivated) { //permission denied
//                naverMap.locationTrackingMode = LocationTrackingMode.None
//            }
//            return
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }

//    override fun onMapReady(naverMap: NaverMap) {
//        this.naverMap = naverMap
//        naverMap.locationSource = locationSource
//    }
//
//    companion object{
//        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
//    }
}