package com.example.hearandthere_test.model.response

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList

data class ResNearestAudioTrackInfoDto(
    val audioFileUrl: String?,
    val audioTrackId: Int,
    val images: ArrayList<String>?,
    val placeAddress: String?,
    val placeName: String?,
    val runningTime: String?,
    val title: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.createStringArrayList(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(audioFileUrl)
        parcel.writeInt(audioTrackId)
        parcel.writeStringList(images)
        parcel.writeString(placeAddress)
        parcel.writeString(placeName)
        parcel.writeString(runningTime)
        parcel.writeString(title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ResNearestAudioTrackInfoDto> {
        override fun createFromParcel(parcel: Parcel): ResNearestAudioTrackInfoDto {
            return ResNearestAudioTrackInfoDto(parcel)
        }

        override fun newArray(size: Int): Array<ResNearestAudioTrackInfoDto?> {
            return arrayOfNulls(size)
        }
    }
}