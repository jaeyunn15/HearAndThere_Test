package com.example.hearandthere_test.network.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class StringConverter {

    private val gson = Gson()
    private val type: Type = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun fromListToString(value: List<String>): String {
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun fromStringToList(value: String): List<String> {
        return gson.fromJson(value, type)
    }
}