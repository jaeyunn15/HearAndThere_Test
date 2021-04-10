package com.example.hearandthere_test.model.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class ResSingleAudioGuideDetailDto(
    val audioGuideId: Int,
    val coursesList: List<ResAudioCourseInfoItemDto>,
    val distance: String,
    val estimatedTravelTime: String,
    val guideImages: List<String>,
    val language: String,
    val location: String,
    val overviewDescription: String,
    val recommendedAudioGuidesList: List<ResAudioGuideItemDto>,
    val recommendedContentsList: @RawValue List<Any>?,
    val tags: List<String>,
    val title: String,
    val tracksList: List<ResAudioTrackInfoItemDto>
) : Parcelable

