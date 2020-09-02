package com.example.hearandthere_test.model.response

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "AudioTrackInfoItem")
data class ResAudioTrackInfoItemDto (
    @PrimaryKey @SerializedName("audioTrackId") val audioTrackId: Int,
    @SerializedName("audioFileUrl") val audioFileUrl: String?,
    @SerializedName("images") val images: List<String>?,
    @SerializedName("placeAddress") val placeAddress: String?,
    @SerializedName("placeName") val placeName: String?,
    @SerializedName("runningTime") val runningTime: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("trackOrderNumber") val trackOrderNumber: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(audioTrackId)
        parcel.writeString(audioFileUrl)
        parcel.writeStringList(images)
        parcel.writeString(placeAddress)
        parcel.writeString(placeName)
        parcel.writeString(runningTime)
        parcel.writeString(title)
        parcel.writeInt(trackOrderNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ResAudioTrackInfoItemDto> {
        override fun createFromParcel(parcel: Parcel): ResAudioTrackInfoItemDto {
            return ResAudioTrackInfoItemDto(parcel)
        }

        override fun newArray(size: Int): Array<ResAudioTrackInfoItemDto?> {
            return arrayOfNulls(size)
        }
    }
}