package com.example.hearandthere_test.model.response

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "AudioTrackInfoItem")
data class ResAudioTrackInfoItemDto (
    val audioFileUrl: String,
    @PrimaryKey val audioTrackId: Int,
    val images: List<String>,
    val runningTime: String,
    val title: String,
    val trackOrderNumber: Int
) : Parcelable