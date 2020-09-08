package com.example.hearandthere_test.ui.mapUtil

object MapState {
    const val LOCATION_REQUEST_INTERVAL = 5000
    const val PERMISSION_REQUEST_CODE = 100

    var trackingEnabled: Boolean = false
    var nearAudioGuideEnabled: Boolean = false
    var locationEnabled: Boolean = false
    var waiting: Boolean = false
}