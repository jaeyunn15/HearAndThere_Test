package com.example.hearandthere_test.util

import android.media.MediaPlayer
import com.example.hearandthere_test.service.AudioService

class PlayBackState {
    companion object Static{
        var SERVICE: AudioService?=null
        var MediaPlayers : ArrayList<MediaPlayer> = arrayListOf()
    }
}