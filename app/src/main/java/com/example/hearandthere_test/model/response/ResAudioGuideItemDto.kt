package com.example.hearandthere_test.model.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ResAudioGuideItemDto (
    val audioGuideId: Int,
    val tags: List<String>,
    val thumbnailImageUrl: String,
    val title: String
) : Parcelable