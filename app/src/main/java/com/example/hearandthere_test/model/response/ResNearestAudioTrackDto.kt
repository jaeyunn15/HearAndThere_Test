package com.example.hearandthere_test.model.response

data class ResNearestAudioTrackDto(
    val audioGuideId: Int,
    val isAudioTrackNearBy: Boolean,
    val nearestTrackInfo: ResNearestAudioTrackInfoDto,
    val userPresentLatitude: Double,
    val userPresentLongitude: Double
)