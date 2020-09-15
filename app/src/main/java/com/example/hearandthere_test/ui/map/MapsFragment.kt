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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.hearandthere_test.R
import com.example.hearandthere_test.databinding.FragmentMapsBinding
import com.example.hearandthere_test.injection.Injection
import com.example.hearandthere_test.model.response.ResAudioTrackInfoItemDto
import com.example.hearandthere_test.service.AudioService
import com.example.hearandthere_test.service.LocationService
import com.example.hearandthere_test.ui.adapter.MapsViewPagerAdapter
import com.example.hearandthere_test.util.MapState
import com.example.hearandthere_test.ui.viewmodel.AudioViewModel
import com.example.hearandthere_test.util.AudioUiHelper
import com.example.hearandthere_test.util.MusicChangedStatus
import com.example.hearandthere_test.util.MusicState
import com.example.hearandthere_test.util.MusicState.PARAM_MUSIC_LIST_BY_TRACK
import com.example.hearandthere_test.util.PlayBackState
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.*
import kotlinx.android.synthetic.main.fragment_maps.view.*
import kotlin.collections.ArrayList

class MapsFragment : Fragment(), OnMapReadyCallback{

    private var musicPlayer : MediaPlayer? = null
    private var musicStatus: MusicChangedStatus = MusicChangedStatus.STOP
    private lateinit var marker : Marker

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
        if (!chkIsPlaying()){
            initSetting()
            dataSetting()
            attachOnClick()
            initMapSetting()
            initMusicReceiver()
        }
        return fragmentView
    }

    private fun setMap(latitude: Double, longitude: Double){
        val coord = LatLng(latitude, longitude)
        val locationOverlay = map.locationOverlay
        locationOverlay.position = coord
        locationOverlay.isVisible = true
        map.moveCamera(CameraUpdate.scrollTo(coord))
    }

    private fun initSetting(){
        fab = fragmentView.findViewById(R.id.fab)
        vp = fragmentView.findViewById(R.id.vp_mapfragment_audioInfo)
        view_nowplay = fragmentView.findViewById<View>(R.id.view_bottom_now_play)
        btn_PausePlay = view_nowplay.findViewById(R.id.btn_now_play_pause)
        btn_NextAudio = view_nowplay.findViewById(R.id.btn_now_play_skipforward)
        btn_PrevAudio = view_nowplay.findViewById(R.id.btn_now_play_skipback)
        sb_audioPlay = view_nowplay.findViewById(R.id.sb_audio_play)
        tv_audioTitle = view_nowplay.findViewById(R.id.tv_now_play_title)
        tv_audioPlaytime = view_nowplay.findViewById(R.id.tv_now_play_time)
        handler = Handler()
        trackAudioList = ArrayList()
        trackAudioIntent = Intent(context, AudioService::class.java)
        locationServiceIntent = Intent(context, LocationService::class.java)
        audioUiHelper = AudioUiHelper()
        sb_audioPlay.max = 100
    }

    private fun dataSetting(){
        audioViewModel = Injection.provideAudioViewModel()
        getData() //get the Data
        observeData() //set the Data
    }

    private fun getData(){
        audioViewModel.getAudioGuideByAudioGuideId(9)
    }

    private fun getDirection(){
        audioViewModel.getTrackDirections(9)
    }

    private fun observeDirection(){
        val polyline = PolylineOverlay()
        val polyArrayList : ArrayList<LatLng> = arrayListOf()
        audioViewModel.audioTrackDirectionsLiveData.observe(viewLifecycleOwner, Observer {
            it.directions.forEach { pData -> polyArrayList.add(LatLng(pData.latitude, pData.longitude)) }
            val polyList: List<LatLng> = polyArrayList.toList()
            polyline.coords = polyList
            polyline.color = Color.RED
            polyline.width = 10
            polyline.map = map
        })
    }

    private fun observeData(){
        audioViewModel.audioResponseLiveData.observe(viewLifecycleOwner, Observer { it ->
            mapsContentAdapter = MapsViewPagerAdapter(this, it.audioTrackInfoList)
            it.run {
                this.audioTrackInfoList.forEach { audio ->
                    trackAudioList.add(audio)
                }
                putDataToService() //put the Data to Service when start fragment
                drawMarker()
            }

            fragmentView.vp_mapfragment_audioInfo.let { vp ->
                vp.adapter = mapsContentAdapter
                vp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                vp.offscreenPageLimit = 3
                vp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                vp.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        trackAudioList.forEach {
                            if (it.trackOrderNumber == position + 1) {
                                val movedMarkPos = LatLng(it.trackLatitude, it.trackLongitude)
                                drawMarker()
                                clickedMarker(
                                    it.trackLatitude,
                                    it.trackLongitude,
                                    it.trackOrderNumber
                                )
                                map.moveCamera(CameraUpdate.scrollTo(movedMarkPos))
                            }
                        }
                    }
                })
            }
        })
    }

    private fun attachOnClick(){
        btn_PausePlay.setOnClickListener {
            when (musicStatus) {
                MusicChangedStatus.STOP -> {
                    play()
                }
                MusicChangedStatus.PLAY -> {
                    pause()
                }
                MusicChangedStatus.PAUSE -> {
                    play()
                }
                else -> {
                    //todo
                }
            }
        }
        btn_NextAudio.setOnClickListener { next() }
        btn_PrevAudio.setOnClickListener { prev() }
    }

    private fun initMapSetting(){
        val mapFragment = requireFragmentManager().findFragmentById(R.id.maps_frag_view) as MapFragment?
            ?: MapFragment.newInstance().also {
                requireFragmentManager().beginTransaction().add(R.id.maps_frag_view, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    private fun initMusicReceiver(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(MusicState.ACTION_STATUS_MUSIC_PLAY)
            intentFilter.addAction(MusicState.ACTION_STATUS_MUSIC_PAUSE)
            intentFilter.addAction(MusicState.ACTION_STATUS_MUSIC_DURATION)
            intentFilter.addAction(MusicState.MUSIC_INFO)
            intentFilter.addAction(MusicState.PLAYLIST_UP)
            intentFilter.addAction(MusicState.ACTION_STATUS_MUSIC_COMPLETE)
            intentFilter.addAction(MusicState.ACTIVITY_REFRESH)
            intentFilter.addAction(MapState.BR_LOCATION)
            intentFilter.addAction(MusicState.MUSIC_VP_CHANGE)

            context!!.registerReceiver(MusicReceiver(), intentFilter)
        }
    }

    private fun putDataToService(){
        trackAudioIntent.putParcelableArrayListExtra(PARAM_MUSIC_LIST_BY_TRACK, trackAudioList)
        MapState.nearAudioGuideEnabled = false // 트랙 기반 재생
        MapState.IS_NOW_NEAR_AUDIO_PLAY = false // 현재 재생 : 트랙 기반
        context?.startService(trackAudioIntent)
    }

    private fun updateVPposition(){
        view_nowplay.visibility = View.VISIBLE
        val mLayoutParams : CoordinatorLayout.LayoutParams = vp.layoutParams as CoordinatorLayout.LayoutParams
        mLayoutParams.bottomMargin = 150
        vp.layoutParams = mLayoutParams
    }

    private fun drawMarker(){
        trackAudioList.forEach { item ->
            marker = Marker()
            marker.let {
                it.position = LatLng(item.trackLatitude, item.trackLongitude)
                val name = "mark_${item.trackOrderNumber}"
                val drawRes = this.resources.getIdentifier(name, "drawable", requireContext().packageName)
                it.icon = OverlayImage.fromResource(drawRes)
                it.tag = item.trackOrderNumber
                it.onClickListener(this, marker.position, item.trackOrderNumber)
                it.map = map
            }
        }
    }

    private fun clickedMarker(latitude: Double, longitude: Double, position: Int){
        val name = "mark_"+position+"_selected"
        val drawRes = this.resources.getIdentifier(name, "drawable", requireContext().packageName)
        marker = Marker()
        marker.let {
            it.position = LatLng(latitude, longitude)
            it.icon = OverlayImage.fromResource(drawRes)
            it.map = map
        }
    }

    private operator fun Overlay.OnClickListener?.invoke(
        mapsFragment: MapsFragment,
        markerPosition: LatLng,
        num: Int
    ) {
        mapsFragment.marker.setOnClickListener {
            if (!vp.isShown){ vp.visibility = View.VISIBLE }
            vp.currentItem = (it.tag) as Int -1
            clickedMarker(markerPosition.latitude, markerPosition.longitude, num) //change marker color
            return@setOnClickListener true
        }
    }

    //ViewPager item별 onClick
    fun clickListener(position: Int, lati: Double, longi: Double){
        clickedMarker(lati, longi, position + 1)
        fab.setImageResource(R.drawable.ic_my_loc)
        MapState.PLAYING_NUM = -1
        MapState.IS_NOW_NEAR_AUDIO_PLAY = false

        if(PlayBackState.chkIsPlay) { //재생중
            hideNowPlayView()
            handler.removeCallbacks(updater)
            optMusic(MusicState.ACTION_LOCATION_BASE_PLAY_STOP)
        }

        putDataToService() //viewpager item on click 모드 체인지.

        context!!.sendBroadcast(
            Intent(MusicState.ACTION_TRACK_BASE_PLAY).putExtra(
                "position",
                position
            )
        )
        updateVPposition()
        Log.d("PLAY AUDIO ", "$position Clicked!!")

    }

    override fun onStart() {
        super.onStart()
        context?.startService(locationServiceIntent)
    }

    override fun onDestroy() {
        context?.stopService(locationServiceIntent)
        super.onDestroy()
    }


    private fun firstStartCameraPosition(){
        val coord = LatLng(37.58093, 126.984838)
        map.moveCamera(CameraUpdate.scrollTo(coord))
    }

    override fun onMapReady(naverMap: NaverMap) {
        map = naverMap
        drawPolyline()
        firstStartCameraPosition()
        mapAttachClickListener()
    }

    private fun drawPolyline(){
        getDirection()
        observeDirection()
    }

    private fun mapAttachClickListener(){
        val locationService = LocationService()
        fab.setOnClickListener {
            if (MapState.LOCATION_TRACE_ON){
                Log.d("오디오 테스트 ", "현재 위치추적 켜져있어서 끔")
                locationService.stopLocationTrace()

            }else if (!MapState.LOCATION_TRACE_ON){
                Log.d("오디오 테스트 ", "현재 위치추적 꺼져있어서 킴")
                locationService.startLocationTrace()
            }
        }
        map.setOnMapClickListener { _, _ ->
            if (vp.isShown){ vp.visibility = View.GONE }
            drawMarker()
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
    private fun updateMusicDurationInfo(totalDuration: Int, currentTime: Int) {
        val totalPlayTime = audioUiHelper.milliSecondsToTimer(totalDuration)
        val nowPlayTime = audioUiHelper.milliSecondsToTimer(currentTime)
        tv_audioPlaytime.text = "$nowPlayTime / $totalPlayTime"
    }

    fun hideNowPlayView(){
        if (view_nowplay.isShown){ view_nowplay.visibility = View.GONE }
    }

    private fun updateSeekBar(){
        if (musicPlayer?.currentPosition == null || musicPlayer?.duration == null){
            //pass
        }else{
            sb_audioPlay.progress = ( (musicPlayer?.currentPosition!! * 100) / musicPlayer?.duration!!)
            handler.postDelayed(updater, 1000)
        }
    }

    @SuppressLint("SetTextI18n")
    private val updater : Runnable = Runnable {
        kotlin.run {
            updateSeekBar()
            val now = audioUiHelper.milliSecondsToTimer(musicPlayer?.currentPosition!!)
            val total = audioUiHelper.milliSecondsToTimer(musicPlayer?.duration!!)
            tv_audioPlaytime.text = now+" / "+total
        }
    }

    private fun updateTitle(trackTitle: String){
        tv_audioTitle.text = trackTitle
    }

    inner class MusicReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == MusicState.ACTION_STATUS_MUSIC_PLAY) {
                btn_PausePlay.setImageResource(R.drawable.pause)
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
                updateTitle(title1!!)

            } else if (action == MusicState.ACTION_STATUS_MUSIC_COMPLETE){ //트랙 종료되면
                if (!MapState.locationEnabled) {  // 위치 추적 꺼져있다면
                    hideNowPlayView()
                    handler.removeCallbacks(updater)
                }else if (MapState.locationEnabled){ //위치 추적 켜져있다면
                    hideNowPlayView()
                    handler.removeCallbacks(updater)
                }
            } else if (action == MapState.BR_LOCATION){
                val lati = intent.getDoubleExtra("Latitude", 0.0)
                val longi = intent.getDoubleExtra("Longitude",0.0)
                setMap(lati, longi)
                Log.d("오디오 브로드 @@","$lati $longi")
            } else if (action == MusicState.MUSIC_VP_CHANGE){
                val idx = intent.getIntExtra(MusicState.MUSIC_VP_CHANGE_IDX, 0) //이게 더 큰거
                vp.currentItem = idx-1
            }
        }
    }

    companion object {
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
        private lateinit var trackAudioIntent : Intent
        private lateinit var locationServiceIntent: Intent
        private lateinit var trackAudioList : ArrayList<ResAudioTrackInfoItemDto>
        private lateinit var mapsContentAdapter : MapsViewPagerAdapter
        private lateinit var handler : Handler
        private lateinit var audioUiHelper: AudioUiHelper
    }
}