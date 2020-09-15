package com.example.hearandthere_test

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.example.hearandthere_test.service.AudioServiceInterface


class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        context = applicationContext
        serviceInterface = AudioServiceInterface(applicationContext)
    }

    companion object {
        var instance: MyApplication? = null
        var serviceInterface: AudioServiceInterface? = null
        var context : Context? = null
    }
}