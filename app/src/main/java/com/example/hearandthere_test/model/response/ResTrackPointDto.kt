package com.example.hearandthere_test.model.response


data class ResTrackPointDto(
    val trackId: Int,
    val trackLatitude: Double,
    val trackLongitude: Double,
    val trackOrder: Int
)