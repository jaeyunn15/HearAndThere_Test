package com.example.hearandthere_test.service

import android.content.Context
import android.media.MediaPlayer
import com.example.hearandthere_test.MyApplication

class MyMediaPlayer(context: Context) : MediaPlayer() {

    @Volatile
    private var instance: MyMediaPlayer? = null
    var mp: MediaPlayer? = null

    fun getInstance(context: Context): MyMediaPlayer? {
        if (instance == null) {
            synchronized(MyMediaPlayer::class.java) {
                if (instance == null) {
                    instance = MyMediaPlayer(context)
                }
            }
        }
        return instance
    }
}