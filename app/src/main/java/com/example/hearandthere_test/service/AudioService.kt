package com.example.hearandthere_test.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.hearandthere_test.model.response.ResAudioTrackInfoItemDto
import com.example.hearandthere_test.model.response.ResNearestAudioTrackInfoDto
import com.example.hearandthere_test.ui.mapUtil.MapState
import com.example.hearandthere_test.util.PlayBackState
import com.example.hearandthere_test.util.MusicState


@Suppress("UNCHECKED_CAST")
class AudioService : Service(), MediaPlayer.OnCompletionListener{

    private var isPrepared = false
    private var isPlayed = false
    private var mIsMusicPause = false
    private var binder: IBinder = AudioServiceBinder()
    private var trackAudioList = ArrayList<ResAudioTrackInfoItemDto>()
    private var nearAudioList = ArrayList<ResNearestAudioTrackInfoDto>()
    private val mMusicReceiver = MusicReceiver()
    private var mIsMusicPLayingNow = false
    private var MUSIC_URL : String? = null
    private var mCurrentMusicIndex = 0
    var mediaState = false //default false
    private var pausePosition : Int? = null
    private var mPlayer : MediaPlayer = MediaPlayer()
    var mediaPlayers : ArrayList<MediaPlayer> = arrayListOf()
    private lateinit var nearAudioDatas: ArrayList<ResNearestAudioTrackInfoDto>
    private lateinit var trackAudioDatas: ArrayList<ResAudioTrackInfoItemDto>
    private lateinit var distinctList : MutableList<ResNearestAudioTrackInfoDto>

    inner class AudioServiceBinder : Binder(){
        fun getService() : AudioService {
            return AudioService()
        }
    }
    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        initBoardCastReceiver()
    }

    private fun initBoardCastReceiver(){
        val intentFilter = IntentFilter()
        intentFilter.addAction(MusicState.ACTION_MUSIC_PLAY)
        intentFilter.addAction(MusicState.ACTION_MUSIC_PAUSE)
        intentFilter.addAction(MusicState.ACTION_MUSIC_NEXT)
        intentFilter.addAction(MusicState.ACTION_MUSIC_LAST)
        intentFilter.addAction(MusicState.ACTION_MUSIC_STOP)
        intentFilter.addAction(MusicState.ACTION_MUSIC_SEEK_TO)
        intentFilter.addAction(MusicState.BS_ENTRY)
        intentFilter.addAction(MusicState.BS_REOPEN)
        intentFilter.addAction(MusicState.MUSIC_NOW_PLAYING)
        registerReceiver(mMusicReceiver, intentFilter)
        distinctList = ArrayList()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        initMusicDatas(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initMusicDatas(intent: Intent){
        if (MapState.nearAudioGuideEnabled){ //주변 오디오 있다면
            nearAudioDatas = intent.getParcelableArrayListExtra(MusicState.PARAM_MUSIC_LIST_BY_LOCATION)
            distinctList = nearAudioDatas
            distinctList = distinctList.toSet().toMutableList()
            distinctList.forEach {
                if (it.audioFileUrl == nearAudioDatas[0].audioFileUrl){
                    //pass
                }else{
                    nearAudioList.addAll(nearAudioDatas)
                }
            }
            Log.d("오디오 서비스 위치!!", "${distinctList.size}")
        }else{ // 주변 오디오 없다면
            trackAudioDatas = intent.getParcelableArrayListExtra(MusicState.PARAM_MUSIC_LIST_BY_TRACK)
            trackAudioList.addAll(trackAudioDatas)
            Log.d("오디오 서비스 트랙!!", "${trackAudioList.size}")
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        stopSelf()
        isPlayed = false
        pausePosition = null
        PlayBackState.MediaPlayers.clear()
    }

    private fun play(index: Int) {
        if (index >= trackAudioList.size) {
            return
        }

        if (nearAudioList.isNotEmpty()){
            if (index>=nearAudioList.size){
                return
            }
        }

        if (mCurrentMusicIndex == index && mIsMusicPause) {
            mPlayer.start()
        } else {
            mPlayer.reset()
            try {
                MUSIC_URL = trackAudioList[index].audioFileUrl
                mPlayer.setDataSource(MUSIC_URL)
                mPlayer.setOnPreparedListener(mPrepareListener)
                mPlayer.prepareAsync()

            } catch (e: Exception) {
                e.printStackTrace()
            }
            mCurrentMusicIndex = index
            mIsMusicPause = false
        }


        mIsMusicPLayingNow = true
        sendMusicStatusBroadCast(MusicState.ACTION_STATUS_MUSIC_PLAY)
        PlayBackState.MediaPlayers.add(mPlayer)
    }

    private var mPrepareListener : MediaPlayer.OnPreparedListener = MediaPlayer.OnPreparedListener {
        it.start()
        val duration: Int = mPlayer.duration
        val currentPosition : Int = mPlayer.currentPosition
        sendMusicDurationBroadCast(duration,currentPosition)
        sendMusicInfoBroadCast(mCurrentMusicIndex)
        sendNowPlayAudioIndex(mCurrentMusicIndex)
    }

    private fun pause() {
        mPlayer.pause()
        mIsMusicPause = true
        mIsMusicPLayingNow = false
        sendMusicStatusBroadCast(MusicState.ACTION_STATUS_MUSIC_PAUSE)
    }

    private fun stop() {
        mPlayer.stop()
        mIsMusicPLayingNow = false
    }

    private fun next() {
        if (mCurrentMusicIndex + 1 < trackAudioList.size) {
            play(mCurrentMusicIndex + 1)
            mIsMusicPLayingNow = true
            sendMusicInfoBroadCast(mCurrentMusicIndex + 1)
        }
    }

    private fun last() {
        if (mCurrentMusicIndex != 0) {
            play(mCurrentMusicIndex - 1)
            mIsMusicPLayingNow = true
            sendMusicInfoBroadCast(mCurrentMusicIndex - 1)
        }
    }

    private fun seekTo(intent: Intent) {
        if (mPlayer.isPlaying) {
            val position = intent.getIntExtra(MusicState.PARAM_MUSIC_SEEK_TO, 0)
            mPlayer.seekTo(position)
            mIsMusicPLayingNow = true
        }
    }

    private fun sendMusicCompleteBroadCast() {
        val intent = Intent(MusicState.ACTION_STATUS_MUSIC_COMPLETE)
        intent.putExtra(MusicState.PARAM_MUSIC_IS_OVER, mCurrentMusicIndex == trackAudioList.size - 1)
        sendBroadcast(intent)
    }


    private fun sendMusicDurationBroadCast(duration: Int, currentPosition : Int) {
        val intent = Intent(MusicState.ACTION_STATUS_MUSIC_DURATION)
        intent.putExtra(MusicState.PARAM_MUSIC_DURATION, duration)
        intent.putExtra(MusicState.PARAM_MUSIC_CURRENT_POSITION, currentPosition)
        sendBroadcast(intent)
    }

    private fun sendMusicStatusBroadCast(action: String) {
        val intent = Intent(action)
        if (action == MusicState.ACTION_STATUS_MUSIC_PLAY) {
            intent.putExtra(MusicState.PARAM_MUSIC_CURRENT_POSITION, mPlayer.currentPosition)
            intent.putExtra(MusicState.PARAM_MUSIC_DURATION, mPlayer.duration)
        }
        sendBroadcast(intent)
    }

    private fun sendMusicInfoBroadCast(index: Int) {
        val title = trackAudioList[index].title
        val title2 = trackAudioList[index].placeName

        val intent = Intent(MusicState.MUSIC_INFO)
        intent.putExtra(MusicState.MUSIC_INFO_TRACK_TITLE, title)
        intent.putExtra(MusicState.MUSIC_INFO_AUDIO_TITLE, title2)
        sendBroadcast(intent)
    }

    private fun sendNowPlayAudioIndex(index:Int){
        val intent = Intent(MusicState.NOWPLAY_MUSIC_IDX)
        intent.putExtra(MusicState.NOWPLAY_MUSIC_IDX_VALUE, index)
        sendBroadcast(intent)
    }

    inner class MusicReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                MusicState.ACTION_MUSIC_PLAY -> { play(mCurrentMusicIndex) }
                MusicState.ACTION_MUSIC_PAUSE -> { pause() }
                MusicState.ACTION_MUSIC_LAST -> { last() }
                MusicState.ACTION_MUSIC_NEXT -> { next() }
                MusicState.ACTION_MUSIC_STOP -> { stop() }
                MusicState.ACTION_MUSIC_SEEK_TO -> { seekTo(intent) }
                MusicState.BS_REOPEN -> { sendMusicInfoBroadCast(mCurrentMusicIndex) }
                MusicState.MUSIC_NOW_PLAYING -> { sendNowPlayAudioIndex(mCurrentMusicIndex) }
            }
        }
    }

    override fun onCompletion(p0: MediaPlayer?) {
        sendMusicCompleteBroadCast()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer.stop()
        mPlayer.release()
        isPlayed = false
        isPrepared = false
        pausePosition = null
        unregisterReceiver(MusicReceiver())
    }
}