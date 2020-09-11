package com.example.hearandthere_test.util

import android.media.MediaPlayer
import com.example.hearandthere_test.service.AudioService

class PlayBackState {
    companion object Static{
        var SERVICE: AudioService?=null
        var mIsMusicPLayingNow = false
        var chkIsPlay = false

        var mIsMusicPause = false

        var CHECK_IS_PLAY_AUDIO = false

        var MediaPlayers : ArrayList<MediaPlayer> = arrayListOf()
    }
}