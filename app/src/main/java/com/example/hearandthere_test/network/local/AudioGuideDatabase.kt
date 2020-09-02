package com.example.hearandthere_test.network.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hearandthere_test.model.response.ResAudioTrackInfoItemDto
import com.example.hearandthere_test.model.response.ResAudioTrackInfoListDto
import com.example.hearandthere_test.network.local.converter.AudioListConverter
import com.example.hearandthere_test.network.local.converter.StringConverter


@Database(entities = [ResAudioTrackInfoListDto::class, ResAudioTrackInfoItemDto::class], version = 2, exportSchema = false)
@TypeConverters(*[AudioListConverter::class, StringConverter::class])
abstract class AudioGuideDatabase : RoomDatabase(){

    abstract fun audioGuideDao() : AudioGuideDao

    companion object{

        private var INSTANCE : AudioGuideDatabase? = null

        @Synchronized
        fun getInstance(context: Context) : AudioGuideDatabase {
            if (INSTANCE == null){
                synchronized(this){
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AudioGuideDatabase::class.java,
                        "audio_guide.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE!!
        }
    }

}