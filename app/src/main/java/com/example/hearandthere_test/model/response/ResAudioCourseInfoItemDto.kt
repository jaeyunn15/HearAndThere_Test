package com.example.hearandthere_test.model.response

import android.os.Parcelable
import kotlinx.android.parcel.RawValue

@kotlinx.android.parcel.Parcelize
data class ResAudioCourseInfoItemDto(
    val audioCourseId: Int,
    val courseImages: List<String>,
    val courseOrderNumber: Int,
    val estimatedTravelTime: String,
    val hasRelatedAttraction: Boolean,
    val relatedTourApiAttractionContentId: Int,
    val relatedTourApiAttractionContentTypeId: Int,
    val title: String
) : Parcelable