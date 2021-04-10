package com.example.hearandthere_test.util

object MapState {
    const val PERMISSION_REQUEST_CODE = 1000

    var mLocationPermissionGranted = false
    var nearAudioGuideEnabled: Boolean = false
    var locationEnabled: Boolean = false
    var LOCATION_TRACE_ON = false
    var IS_NOW_NEAR_AUDIO_PLAY = false

    const val BR_LOCATION = "BR_LOCATION"
}