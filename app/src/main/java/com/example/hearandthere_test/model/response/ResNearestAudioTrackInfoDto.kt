package com.example.hearandthere_test.model.response

data class ResNearestAudioTrackInfoDto(
    val audioFileUrl: String,
    val audioTrackId: Int,
    val images: List<String>,
    val placeAddress: String,
    val placeName: String,
    val runningTime: String,
    val title: String
)