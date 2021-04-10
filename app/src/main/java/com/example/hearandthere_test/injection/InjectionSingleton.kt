package com.example.hearandthere_test.injection

import android.media.MediaPlayer

class InjectionSingleton private constructor() {

    companion object {

        @Volatile private var instance: MediaPlayer? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: MediaPlayer().also { instance = it }
            }
    }
}