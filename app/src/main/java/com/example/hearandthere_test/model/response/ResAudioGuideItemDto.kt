package com.example.hearandthere_test.model.response

import android.os.Parcel
import android.os.Parcelable

data class ResAudioGuideItemDto(
    val audioGuideId: Int,
    val imageUrl: String,
    val tags: String,
    val title: String
)