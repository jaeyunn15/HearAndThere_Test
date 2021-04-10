package com.example.hearandthere_test.model.response

data class ResAudioGuideDirectionsDto(
    val audioGuideId: Int,
    val directions: List<ResDirectionDto>,
    val trackPoints: List<ResTrackPointDto>
)

