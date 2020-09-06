package com.example.hearandthere_test.model.response

data class ResNearestAudioTrackDto(
    val audioGuideId: Int,
    val isAudioTrackNearBy: Boolean,
    val resNearestAudioTrackInfoDto: ResNearestAudioTrackInfoDto,
    val userPresentLatitude: Double,
    val userPresentLongitude: Double
)