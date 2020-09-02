package com.example.hearandthere_test.model.response

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hearandthere_test.model.response.ResAudioTrackInfoItemDto
import com.google.gson.annotations.SerializedName

@Entity(tableName = "AudioTrackInfoList")
data class ResAudioTrackInfoListDto (
    @PrimaryKey @SerializedName("audioGuideId") val audioGuideId: Int,
    @SerializedName("audioGuideTitle") val audioGuideTitle: String,
    @SerializedName("audioTrackInfoList") val audioTrackInfoList: List<ResAudioTrackInfoItemDto>
)