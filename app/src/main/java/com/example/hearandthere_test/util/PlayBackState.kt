package com.example.hearandthere_test.util

import android.media.MediaPlayer

class PlayBackState {
    companion object Static{
        var mIsMusicPLayingNow = false
        var chkIsPlay = false
        var mIsMusicPause = false
        var CHECK_IS_PLAY_AUDIO = false
        var MediaPlayers : ArrayList<MediaPlayer> = arrayListOf()
    }
}