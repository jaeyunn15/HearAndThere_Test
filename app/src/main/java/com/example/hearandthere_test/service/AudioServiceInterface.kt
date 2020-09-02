package com.example.hearandthere_test.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

class AudioServiceInterface (context: Context) {
    var mService : AudioService? = null

    init {
        mServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                mService = (service as AudioService.AudioServiceBinder).getService()
            }
            override fun onServiceDisconnected(name: ComponentName) {
                mService = null
            }
        }
        context.bindService(
            Intent(context, AudioService::class.java).setPackage(context.packageName),
            mServiceConnection as ServiceConnection, Context.BIND_AUTO_CREATE
        )
    }

    companion object{
        lateinit var mServiceConnection: ServiceConnection

    }
}