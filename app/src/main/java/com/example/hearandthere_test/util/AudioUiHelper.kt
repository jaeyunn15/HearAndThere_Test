package com.example.hearandthere_test.util

open class AudioUiHelper {

    fun milliSecondsToTimer(milliSeconds: Int) : String? {
        var timerString = ""
        val secondsString : String

        val hours = ((milliSeconds / (1000 * 60 *60 )) % 24 )
        val minutes = ((milliSeconds / (1000 * 60 )) % 60 )
        val seconds = (milliSeconds / 1000 ) % 60 ;

        if (hours > 0){ timerString = "$hours:" }
        secondsString = if (seconds < 10){ "0$seconds" } else{ "$seconds" }
        timerString = "$timerString$minutes:$secondsString"

        return timerString
    }
}