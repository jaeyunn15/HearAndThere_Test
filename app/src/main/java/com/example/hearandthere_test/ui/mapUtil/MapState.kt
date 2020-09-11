package com.example.hearandthere_test.ui.mapUtil

object MapState {
    const val LOCATION_REQUEST_INTERVAL = 10000
    const val PERMISSION_REQUEST_CODE = 100

    var trackingEnabled: Boolean = true //위치 추적의 유
    var nearAudioGuideEnabled: Boolean = false
    var locationEnabled: Boolean = false
    var waiting: Boolean = false

    var IS_NOW_NEAR_AUDIO_PLAY = false

    var PLAYING_NUM = -1
    var PLAYED_NUM = -2

}