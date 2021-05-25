package com.example.hearandthere_test.injection

import android.media.MediaPlayer

object InjectionSingleton {

    //object 키워드로 싱글톤이 제공. thread-safe 하고 lazy 한 초기화 가능
    val getInstance: MediaPlayer
        get() = instance

    private val instance : MediaPlayer = MediaPlayer()

//    companion object {
//
//        @Volatile private var instance: MediaPlayer? = null
//
//        @JvmStatic fun getInstance() =
//            instance ?: synchronized(this) {
//                instance ?: MediaPlayer().also { instance = it }
//            }
//    }

}