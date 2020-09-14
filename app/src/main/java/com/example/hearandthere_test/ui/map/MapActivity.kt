package com.example.hearandthere_test.ui.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.hearandthere_test.R
import com.google.android.gms.common.internal.Preconditions
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource


class MapActivity : FragmentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var fragment = supportFragmentManager.findFragmentById(R.id.container)
        val mapsFragment = MapsFragment()

        if (fragment == null){
            fragment = mapsFragment

            addFragmentToActivity(
                supportFragmentManager,
                fragment,
                R.id.container
            )
        }
    }

    private fun addFragmentToActivity(fragmentManager: FragmentManager, fragment: Fragment, frameId : Int){
        Preconditions.checkNotNull(fragmentManager)
        Preconditions.checkNotNull(fragment)
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(frameId, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}