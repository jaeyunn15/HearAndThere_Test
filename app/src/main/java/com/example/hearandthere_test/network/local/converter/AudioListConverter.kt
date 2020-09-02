package com.example.hearandthere_test.network.local.converter

import androidx.room.TypeConverter
import com.example.hearandthere_test.model.response.ResAudioTrackInfoItemDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class AudioListConverter {

    private val gson = Gson()
    private val type: Type = object : TypeToken<List<ResAudioTrackInfoItemDto?>?>() {}.type

    @TypeConverter
    fun stringToAudioTrackInfo(json : String) : List<ResAudioTrackInfoItemDto> {
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun audioTrackInfoToString(audioTrackInfo: List<ResAudioTrackInfoItemDto>) : String{
        return gson.toJson(audioTrackInfo, type)
    }
}