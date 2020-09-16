package com.example.hearandthere_test.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.example.hearandthere_test.R
import com.example.hearandthere_test.util.MapState


class MapActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermiss()
            return
        }else{
            val mapsFragment = MapsFragment()
            replaceFragment(mapsFragment)
        }
    }

    fun replaceFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commitAllowingStateLoss()
    }

    fun backToMain(){
        onBackPressed()
    }

    private fun requestPermiss(){
        ActivityCompat.requestPermissions(this, MapPermission.PERMISSIONS, 1000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == MapState.PERMISSION_REQUEST_CODE) {
            if (grantResults.all {
                    it == PermissionChecker.PERMISSION_GRANTED
                }) {
                backToMain()
                MapState.mLocationPermissionGranted = true
                Toast.makeText(applicationContext, "Now you can enjoy our app.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, "You can't use our app until you agree with it.",Toast.LENGTH_LONG).show()
                MapState.mLocationPermissionGranted = false
                requestPermiss()
                return
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}