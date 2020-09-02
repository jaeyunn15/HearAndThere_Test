package com.example.hearandthere_test.ui.map

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager2.widget.ViewPager2
import com.example.hearandthere_test.MyApplication
import com.example.hearandthere_test.R
import com.example.hearandthere_test.databinding.FragmentMapsBinding
import com.example.hearandthere_test.injection.Injection
import com.example.hearandthere_test.model.response.ResAudioTrackInfoItemDto
import com.example.hearandthere_test.network.local.AudioGuideDatabase
import com.example.hearandthere_test.network.local.LocalDataSource
import com.example.hearandthere_test.network.local.datasource.AudioGuideLocalDataSource
import com.example.hearandthere_test.network.local.datasource.AudioGuideLocalDataSourceImpl
import com.example.hearandthere_test.network.repository.AudioGuideRepo
import com.example.hearandthere_test.network.repository.AudioGuideRepoImpl
import com.example.hearandthere_test.service.AudioService
import com.example.hearandthere_test.ui.mapUtil.MapPermission
import com.example.hearandthere_test.ui.mapUtil.MapState
import com.example.hearandthere_test.ui.viewmodel.AudioViewModel
import com.example.hearandthere_test.ui.viewmodel.ViewModelFactory
import com.example.hearandthere_test.util.MusicChangedStatus
import com.example.hearandthere_test.util.MusicState
import com.example.hearandthere_test.util.MusicState.PARAM_MUSIC_LIST
import com.example.hearandthere_test.util.PlayBackState
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.MapFragment
import kotlinx.android.synthetic.main.view_audio_nowplay.*

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: NaverMap
    private lateinit var fab : FloatingActionButton
    private lateinit var vp : ViewPager2
    private lateinit var fragmentView : View
    private lateinit var view_nowplay : View
    private lateinit var btn_PausePlay : ImageView
    private lateinit var btn_NextAudio : Button
    private lateinit var btn_PrevAudio : Button
    private lateinit var sb_audioPlay : SeekBar
    private lateinit var tv_audioTitle : TextView
    private lateinit var tv_audioPlaytime : TextView
    private lateinit var audioViewModel : AudioViewModel
    private lateinit var dataIntent : Intent
    lateinit var audioList : List<ResAudioTrackInfoItemDto>
    lateinit var dataList : ArrayList<ResAudioTrackInfoItemDto>
    private lateinit var handler : Handler
    private var musicPlayer : MediaPlayer? = null
    private var musicStatus: MusicChangedStatus = MusicChangedStatus.STOP

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult == null) {
                return
            }
            val lastLocation = locationResult.lastLocation
            val coord = LatLng(lastLocation)
            val locationOverlay = map.locationOverlay
            locationOverlay.position = coord
            locationOverlay.bearing = lastLocation.bearing
            map.moveCamera(CameraUpdate.scrollTo(coord))
            if (MapState.waiting) {
                MapState.waiting = false
                fab.setImageResource(R.drawable.ic_baseline_location_disabled_24)
                locationOverlay.isVisible = true
            }
        }
    }

    private fun chkIsPlaying() : Boolean{
        var chkResult : Boolean = false
        for (mediaPlayer in PlayBackState.MediaPlayers) {
            chkResult = mediaPlayer.isPlaying
            if (chkResult){
                musicPlayer = mediaPlayer
                updateMusicDurationInfo(mediaPlayer.duration, mediaPlayer.currentPosition)
                updateSeekBar()
            }
        }
        return chkResult
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentMapsBinding = DataBindingUtil.inflate<FragmentMapsBinding>(
            inflater,
            R.layout.fragment_maps,
            container,
            false
        )
        fragmentView = fragmentMapsBinding.root

        if (chkIsPlaying()){

        }else{
            initSetting()
            dataSetting()
            attachOnClick()
            initMapSetting()
            initMusicReceiver()
            tryEnableLocation()
        }

        return fragmentView
    }

    private fun dataSetting(){
        audioViewModel = Injection.provideAudioViewModel()
        getData() //데이터 먼저 가져와야지
        observeData() //가져온 데이터 설정으로 넣어놔야
    }

    private fun initSetting(){
        fab = fragmentView.findViewById(R.id.fab)
        vp = fragmentView.findViewById<ViewPager2>(R.id.vp_mapfragment_audioInfo)
        view_nowplay = fragmentView.findViewById<View>(R.id.view_bottom_now_play)
        btn_PausePlay = view_nowplay.findViewById(R.id.btn_now_play_pause)
        btn_NextAudio = view_nowplay.findViewById(R.id.btn_now_play_skipforward)
        btn_PrevAudio = view_nowplay.findViewById(R.id.btn_now_play_skipback)
        sb_audioPlay = view_nowplay.findViewById(R.id.sb_audio_play)
        tv_audioTitle = view_nowplay.findViewById(R.id.tv_now_play_title)
        tv_audioPlaytime = view_nowplay.findViewById(R.id.tv_now_play_time)
        handler = Handler()
        dataList = ArrayList()
        audioList = ArrayList()
        dataIntent = Intent(context, AudioService::class.java)
        sb_audioPlay.max = 100
    }

    private fun attachOnClick(){
        view_nowplay.setOnClickListener {
            if (vp.isShown){
                vp.visibility = View.GONE
            }else{
                vp.visibility = View.VISIBLE
            }
        }
        btn_PausePlay.setOnClickListener {
            if (musicStatus == MusicChangedStatus.STOP){
                play()
            }else if (musicStatus == MusicChangedStatus.PLAY){
                pause()
            }else if (musicStatus == MusicChangedStatus.PAUSE){
                play()
            }
        }
        btn_NextAudio.setOnClickListener {
            next()
        }
        btn_PrevAudio.setOnClickListener {
            prev()
        }
    }

    private fun getData(){
        audioViewModel.getAudioGuideByAudioGuideId(1)
    }

    private fun observeData(){
        audioViewModel.audioResponseLiveData.observe(this, Observer {
            audioList = it.audioTrackInfoList
            it.run {
                this.audioTrackInfoList.forEach { audio ->
                    dataList.add(audio)
                }
                putDataToService()
            }
        })
    }

    private fun putDataToService(){
        dataIntent.putParcelableArrayListExtra(PARAM_MUSIC_LIST, dataList)
        if (dataList.isNotEmpty()){
            context?.startService(dataIntent)
        }
    }

    private fun milliSecondsToTimer(milliSeconds: Int) : String? {
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

    private fun initMusicReceiver(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(MusicState.ACTION_STATUS_MUSIC_PLAY)
            intentFilter.addAction(MusicState.ACTION_STATUS_MUSIC_PAUSE)
            intentFilter.addAction(MusicState.ACTION_STATUS_MUSIC_DURATION)
            intentFilter.addAction(MusicState.MUSIC_INFO)
            intentFilter.addAction(MusicState.PLAYLIST_UP)
            context!!.registerReceiver(MusicReceiver(), intentFilter) //정적으로 리시버 등록
        }
    }

    private fun initMapSetting(){
        val mapFragment = requireFragmentManager().findFragmentById(R.id.maps_frag_view) as MapFragment?
            ?: MapFragment.newInstance().also {
                requireFragmentManager().beginTransaction().add(R.id.maps_frag_view, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MapState.PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PermissionChecker.PERMISSION_GRANTED }) {
                enableLocation()
            } else {
                fab.setImageResource(R.drawable.ic_baseline_my_location_24)
            }
            return
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        if (MapState.trackingEnabled) {
            enableLocation()
        }
    }

    override fun onStop() {
        super.onStop()
        disableLocation()
    }

    override fun onMapReady(naverMap: NaverMap) {
        map = naverMap

        fab.setOnClickListener {
            if (MapState.trackingEnabled){
                disableLocation()
                fab.setImageResource(R.drawable.ic_baseline_my_location_24)
            }else{
                fab.setImageDrawable(CircularProgressDrawable(requireContext()).apply {
                    setStyle(CircularProgressDrawable.LARGE)
                    setColorSchemeColors(Color.WHITE)
                    start()
                })
                tryEnableLocation()
            }
            MapState.trackingEnabled = !MapState.trackingEnabled
        }
    }

    private fun tryEnableLocation() {
        if (MapPermission.PERMISSIONS.all { ContextCompat.checkSelfPermission(requireContext(), it) == PermissionChecker.PERMISSION_GRANTED }) {
            enableLocation()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), MapPermission.PERMISSIONS, MapState.PERMISSION_REQUEST_CODE)
        }
    }

    private fun enableLocation() {
        GoogleApiClient.Builder(requireContext())
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                @SuppressLint("MissingPermission")
                override fun onConnected(bundle: Bundle?) {
                    val locationRequest = LocationRequest().apply {
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = MapState.LOCATION_REQUEST_INTERVAL.toLong()
                        fastestInterval = MapState.LOCATION_REQUEST_INTERVAL.toLong()
                    }

                    LocationServices.getFusedLocationProviderClient(requireContext())
                        .requestLocationUpdates(locationRequest, locationCallback, null)
                    MapState.locationEnabled = true
                    MapState.waiting = true

                }
                override fun onConnectionSuspended(i: Int) {
                }
            })
            .addApi(LocationServices.API)
            .build()
            .connect()
        updateFAB()
    }

    private fun disableLocation() {
        if (!MapState.locationEnabled) {
            return
        }
        LocationServices.getFusedLocationProviderClient(requireContext()).removeLocationUpdates(locationCallback)
        MapState.locationEnabled = false
        updateFAB()
    }

    private fun updateFAB(){
        if (MapState.trackingEnabled){
            fab.setImageResource(R.drawable.ic_baseline_my_location_24)
        }else{
            fab.setImageResource(R.drawable.ic_baseline_location_disabled_24)
        }
    }

    private fun play() {
        optMusic(MusicState.ACTION_MUSIC_PLAY)
        musicStatus = MusicChangedStatus.PLAY
    }

    private fun pause(){
        optMusic(MusicState.ACTION_MUSIC_PAUSE)
        handler.removeCallbacks(updater)
        musicStatus = MusicChangedStatus.PAUSE
    }

    private fun next() {
        optMusic(MusicState.ACTION_MUSIC_NEXT)
        handler.removeCallbacks(updater)
        musicStatus = MusicChangedStatus.PLAY
    }

    private fun prev() {
        optMusic(MusicState.ACTION_MUSIC_LAST)
        handler.removeCallbacks(updater)
        musicStatus = MusicChangedStatus.PLAY
    }

    private fun optMusic(action: String) {
        context!!.sendBroadcast(Intent(action))
    }

    @SuppressLint("SetTextI18n")
    private fun updateMusicDurationInfo(totalDuration: Int, currentTime : Int) {
        val totalPlayTime = milliSecondsToTimer(totalDuration)
        val nowPlayTime = milliSecondsToTimer(currentTime)
        tv_audioPlaytime.text = "$nowPlayTime / $totalPlayTime"
    }

    private fun updateSeekBar(){
        sb_audioPlay.progress = ( (musicPlayer?.currentPosition!! * 100) / musicPlayer?.duration!!)
        handler.postDelayed(updater, 1000)
    }

    @SuppressLint("SetTextI18n")
    private val updater : Runnable = Runnable {
        kotlin.run {
            updateSeekBar()
            val now = milliSecondsToTimer(musicPlayer?.currentPosition!!)
            val total = milliSecondsToTimer(musicPlayer?.duration!!)
            tv_audioPlaytime.text = now+" / "+total
        }
    }

    private fun updateTitle(trackTitle : String, audioTitle : String){
        tv_audioTitle.text = trackTitle
    }

    inner class MusicReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == MusicState.ACTION_STATUS_MUSIC_PLAY) {
                btn_PausePlay.setImageResource(R.drawable.pause)
                val currentPosition = intent.getIntExtra(MusicState.PARAM_MUSIC_CURRENT_POSITION, 0)
                val duration = intent.getIntExtra(MusicState.PARAM_MUSIC_DURATION, 0)

                if (duration != 0) {
                    for (mediaPlayer in PlayBackState.MediaPlayers) {
                        if (mediaPlayer.isPlaying) {
                            musicPlayer = mediaPlayer
                            updateSeekBar()
                        }
                    }
                }
                musicStatus = MusicChangedStatus.PLAY

            } else if (action == MusicState.ACTION_STATUS_MUSIC_PAUSE) {
                btn_PausePlay.setImageResource(R.drawable.play_button)
                musicStatus = MusicChangedStatus.PAUSE

            } else if (action == MusicState.ACTION_STATUS_MUSIC_DURATION) {
                val duration = intent.getIntExtra(MusicState.PARAM_MUSIC_DURATION, 0)
                val currentPosition = intent.getIntExtra(MusicState.PARAM_MUSIC_CURRENT_POSITION, 0)

                if (duration != 0){
                    for (mediaPlayer in PlayBackState.MediaPlayers){
                        if (mediaPlayer.isPlaying){
                            musicPlayer = mediaPlayer
                            updateSeekBar()
                        }
                    }
                }
                updateMusicDurationInfo(duration, currentPosition)

            } else if (action == MusicState.MUSIC_INFO){
                val title1 = intent.getStringExtra(MusicState.MUSIC_INFO_TRACK_TITLE) //이게 더 큰거
                val title2 = intent.getStringExtra(MusicState.MUSIC_INFO_AUDIO_TITLE)
                updateTitle(title1, title2)
            }
        }
    }
}