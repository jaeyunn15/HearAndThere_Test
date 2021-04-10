package com.example.hearandthere_test.ui.map

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.hearandthere_test.R
import com.example.hearandthere_test.databinding.FragmentMapsBinding
import com.example.hearandthere_test.injection.Injection
import com.example.hearandthere_test.injection.InjectionSingleton
import com.example.hearandthere_test.model.response.ResAudioTrackInfoItemDto
import com.example.hearandthere_test.model.response.ResDirectionDto
import com.example.hearandthere_test.model.response.ResTrackPointDto
import com.example.hearandthere_test.service.AudioService
import com.example.hearandthere_test.service.AudioServiceInterface
import com.example.hearandthere_test.service.LocationService
import com.example.hearandthere_test.ui.adapter.MapsViewPagerAdapter
import com.example.hearandthere_test.ui.error.ErrorFragment
import com.example.hearandthere_test.ui.viewmodel.AudioViewModel
import com.example.hearandthere_test.util.*
import com.example.hearandthere_test.util.MusicState.PARAM_MUSIC_LIST_BY_TRACK
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay

class MapsFragment : Fragment(), OnMapReadyCallback {

    private val mPlayer = InjectionSingleton.getInstance()
    //lateinit var mPlayer : MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_maps, container, false)
        initSetting()
        return binding.root
    }

    private fun initSetting(){
//        mPlayer = AudioService.mPlayer
        if (mPlayer.isPlaying){
            updateMusicDurationInfo(mPlayer.duration, mPlayer.currentPosition)
            updateSeekBar()
        }else{
            initViewSetting()
            viewModelInjection()
            observeAudioData()
            attachOnClick()
            initMapSetting()
            initMusicReceiver()
        }
    }

    private fun viewModelInjection(){
        audioViewModel = Injection.provideAudioViewModel()
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
            }
        }
        btn_NextAudio.setOnClickListener { next() }
        btn_PrevAudio.setOnClickListener { prev() }
        binding.btnGoBackToSplash.setOnClickListener {
            (activity as MapActivity).backToMain()
        }
        binding.viewBottomNowPlay.viewBottomAudioRoot.setOnClickListener {
            if (binding.vpMapfragmentAudioInfo.isShown){ binding.vpMapfragmentAudioInfo.visibility = View.GONE }
            else { binding.vpMapfragmentAudioInfo.visibility = View.VISIBLE }
        }
    }

    private fun initMapSetting(){
        val mapFragment = requireFragmentManager().findFragmentById(R.id.maps_frag_view) as MapFragment?
            ?: MapFragment.newInstance().also {
                requireFragmentManager().beginTransaction().add(R.id.maps_frag_view, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    private fun observeAudioData(){
        audioViewModel.audioResponseLiveData.observe(viewLifecycleOwner, Observer { it ->
            it.run {
                trackAudioList = this.tracksList as ArrayList<ResAudioTrackInfoItemDto>
                putDataToService()
            }
        })
    }

    private fun putDataToService(){
        trackAudioIntent.putParcelableArrayListExtra(PARAM_MUSIC_LIST_BY_TRACK, trackAudioList)
        MapState.nearAudioGuideEnabled = false // 트랙 기반 재생
        MapState.IS_NOW_NEAR_AUDIO_PLAY = false // 현재 재생 : 트랙 기반
        requireContext().startService(trackAudioIntent)
    }

    private fun updateVPposition(){
        binding.viewBottomNowPlay.viewBottomAudioRoot.visibility = View.VISIBLE
        val mLayoutParams : CoordinatorLayout.LayoutParams = binding.vpMapfragmentAudioInfo.layoutParams as CoordinatorLayout.LayoutParams
        mLayoutParams.bottomMargin = 150
        binding.vpMapfragmentAudioInfo.layoutParams = mLayoutParams
    }

    fun clickListener(position: Int, lati: Double, longi: Double){
        clickedMarker(lati, longi, position + 1)
        MapState.IS_NOW_NEAR_AUDIO_PLAY = false

        if(PlayBackState.chkIsPlay) {
            hideNowPlayView()
            handler.removeCallbacks(updater)
            optMusic(MusicState.ACTION_LOCATION_BASE_PLAY_STOP)
        }
        putDataToService()
        requireContext().sendBroadcast(Intent(MusicState.ACTION_TRACK_BASE_PLAY).putExtra("position", position))
        updateVPposition()
    }

    private fun firstStartCameraPosition(){
        val coord = LatLng(37.58093, 126.984838)
        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(coord, 15.0))
    }

    override fun onMapReady(nM: NaverMap) {
        naverMap = nM
        observeMapData()
        firstStartCameraPosition()
        mapAttachClickListener()
    }

    private fun observeMapData(){
        val polyline = PolylineOverlay()
        val polyArrayList : ArrayList<LatLng> = arrayListOf()
        audioViewModel.audioTrackDirectionsLiveData.observe(viewLifecycleOwner, Observer {
            directionsList = it.directions as ArrayList<ResDirectionDto>
            trackPointList = it.trackPoints as ArrayList<ResTrackPointDto>
            mapsContentAdapter = MapsViewPagerAdapter(this, it.trackPoints, trackAudioList)
            it.directions.forEach { pData ->
                polyArrayList.add(
                    LatLng(
                        pData.latitude,
                        pData.longitude
                    )
                )
            }
            val polyList: List<LatLng> = polyArrayList.toList()
            polyline.apply {
                coords = polyList
                color = Color.RED
                width = 10
                map = naverMap
            }

            vpSetting()
            drawMarker()
        })
    }

    fun vpSetting(){
        binding.vpMapfragmentAudioInfo.let { vp ->
            vp.adapter = mapsContentAdapter
            vp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            vp.offscreenPageLimit = 3
            vp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            vp.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    trackPointList.forEach {
                        if (it.trackOrder == position + 1) {
                            val movedMarkPos = LatLng(it.trackLatitude, it.trackLongitude)
                            drawMarker()
                            clickedMarker(
                                it.trackLatitude,
                                it.trackLongitude,
                                it.trackOrder
                            )
                            naverMap.moveCamera(CameraUpdate.scrollTo(movedMarkPos))
                        }
                    }
                }
            })
        }
    }

    private fun drawMarker(){
        trackPointList.forEach { item ->
            marker = Marker()
            marker.let {
                it.position = LatLng(item.trackLatitude, item.trackLongitude)
                val name = "mark_${item.trackOrder}"
                val drawRes = this.resources.getIdentifier(
                    name,
                    "drawable",
                    requireContext().packageName
                )
                it.icon = OverlayImage.fromResource(drawRes)
                it.tag = item.trackOrder
                it.onClickListener(this, marker.position, item.trackOrder)
                it.map = naverMap
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
            it.map = naverMap
        }
    }

    private operator fun Overlay.OnClickListener?.invoke(
        mapsFragment: MapsFragment,
        markerPosition: LatLng,
        num: Int
    ) {
        mapsFragment.marker.setOnClickListener {
            Log.d("플레이어 상태 :: ", mPlayer.isPlaying.toString())
            if (!binding.vpMapfragmentAudioInfo.isShown){ binding.vpMapfragmentAudioInfo.visibility = View.VISIBLE }
            binding.vpMapfragmentAudioInfo.currentItem = (it.tag) as Int -1
            clickedMarker(markerPosition.latitude, markerPosition.longitude, num) //change marker color
            return@setOnClickListener true
        }
    }


    private fun setMap(latitude: Double, longitude: Double){
        val coord = LatLng(latitude, longitude)
        val locationOverlay = naverMap.locationOverlay
        locationOverlay.position = coord
        locationOverlay.isVisible = true
        naverMap.moveCamera(CameraUpdate.scrollTo(coord))
    }

    private fun mapAttachClickListener(){
        binding.fab.setOnClickListener {
            if (MapState.LOCATION_TRACE_ON){
                requireContext().stopService(locationServiceIntent)
                binding.fab.setImageResource(R.drawable.ic_my_loc)

            }else if (!MapState.LOCATION_TRACE_ON){
                requireContext().startService(locationServiceIntent)
                binding.fab.setImageResource(R.drawable.ic_baseline_location_disabled_24)
            }
        }
        naverMap.setOnMapClickListener { _, _ ->
            if (binding.vpMapfragmentAudioInfo.isShown){ binding.vpMapfragmentAudioInfo.visibility = View.GONE }
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
        val totalPlayTime = milliSecondsToTimer(totalDuration)
        val nowPlayTime = milliSecondsToTimer(currentTime)
        binding.viewBottomNowPlay.tvNowPlayTime.text = "$nowPlayTime / $totalPlayTime"
    }

    fun hideNowPlayView(){
        if (binding.viewBottomNowPlay.viewBottomAudioRoot.isShown){ binding.viewBottomNowPlay.viewBottomAudioRoot.visibility = View.GONE }
    }

    private fun updateSeekBar(){
        Thread {
            sb_audioPlay.progress = ( (mPlayer?.currentPosition?.times(100)) / mPlayer?.duration)
            handler.postDelayed(updater, 1000)
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private val updater : Runnable = Runnable {
        updateSeekBar()
        val now = milliSecondsToTimer(mPlayer.currentPosition)
        val total = milliSecondsToTimer(mPlayer.duration)
        tv_audioPlaytime.text = "$now / $total"
    }

    private fun updateTitle(trackTitle: String){
        tv_audioTitle.text = trackTitle
    }

    private fun sendErrorPage(){
        requireContext().stopService(trackAudioIntent)
        (activity as MapActivity).replaceFragment(ErrorFragment.newInstance()!!)
    }

    override fun onDestroy() {
        requireContext().stopService(locationServiceIntent)
        super.onDestroy()
    }

    private fun initMusicReceiver(){
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
        intentFilter.addAction(MusicState.ACTION_STATUS_AUDIO_ERROR)
        requireContext().registerReceiver(MusicReceiver(), intentFilter)
    }

    inner class MusicReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == MusicState.ACTION_STATUS_MUSIC_PLAY) {
                btn_PausePlay.setImageResource(R.drawable.pause)
                val duration = intent.getIntExtra(MusicState.PARAM_MUSIC_DURATION, 0)
                if (duration != 0) if(mPlayer.isPlaying) updateSeekBar()
                musicStatus = MusicChangedStatus.PLAY

            } else if (action == MusicState.ACTION_STATUS_MUSIC_PAUSE) {
                btn_PausePlay.setImageResource(R.drawable.play_button)
                musicStatus = MusicChangedStatus.PAUSE

            } else if (action == MusicState.ACTION_STATUS_MUSIC_DURATION) {
                val duration = intent.getIntExtra(MusicState.PARAM_MUSIC_DURATION, 0)
                val currentPosition = intent.getIntExtra(MusicState.PARAM_MUSIC_CURRENT_POSITION, 0)
                if (duration != 0) if(mPlayer.isPlaying) updateSeekBar()
                updateMusicDurationInfo(duration, currentPosition)

            } else if (action == MusicState.MUSIC_INFO){
                val title1 = intent.getStringExtra(MusicState.MUSIC_INFO_TRACK_TITLE)
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
                val longi = intent.getDoubleExtra("Longitude", 0.0)
                setMap(lati, longi)

            } else if (action == MusicState.MUSIC_VP_CHANGE){
                val idx = intent.getIntExtra(MusicState.MUSIC_VP_CHANGE_IDX, 0)
                binding.vpMapfragmentAudioInfo.currentItem = idx

            } else if (action == MusicState.ACTION_STATUS_AUDIO_ERROR){
                sendErrorPage()
            }
        }
    }

    private fun initViewSetting(){
        handler = Handler(Looper.getMainLooper())
        trackAudioList = ArrayList()
        trackAudioIntent = Intent(requireContext(), AudioService::class.java)
        locationServiceIntent = Intent(requireContext(), LocationService::class.java)
        binding.viewBottomNowPlay.sbAudioPlay.max = 100
        initFindViewById()
    }

    private fun initFindViewById(){
        btn_PausePlay = binding.viewBottomNowPlay.viewBottomAudioRoot.findViewById(R.id.btn_now_play_pause)
        btn_NextAudio = binding.viewBottomNowPlay.viewBottomAudioRoot.findViewById(R.id.btn_now_play_skipforward)
        btn_PrevAudio = binding.viewBottomNowPlay.viewBottomAudioRoot.findViewById(R.id.btn_now_play_skipback)
        sb_audioPlay = binding.viewBottomNowPlay.viewBottomAudioRoot.findViewById(R.id.sb_audio_play)
        tv_audioTitle = binding.viewBottomNowPlay.viewBottomAudioRoot.findViewById(R.id.tv_now_play_title)
        tv_audioPlaytime = binding.viewBottomNowPlay.viewBottomAudioRoot.findViewById(R.id.tv_now_play_time)
    }

    private lateinit var binding : FragmentMapsBinding
    private lateinit var btn_PausePlay : ImageView
    private lateinit var btn_NextAudio : Button
    private lateinit var btn_PrevAudio : Button
    private lateinit var sb_audioPlay : SeekBar
    private lateinit var tv_audioTitle : TextView
    private lateinit var tv_audioPlaytime : TextView

    private lateinit var naverMap: NaverMap
    private lateinit var audioViewModel : AudioViewModel
    private lateinit var trackAudioIntent : Intent
    private lateinit var locationServiceIntent: Intent
    private lateinit var trackAudioList : ArrayList<ResAudioTrackInfoItemDto>
    private lateinit var directionsList : ArrayList<ResDirectionDto>
    private lateinit var trackPointList : ArrayList<ResTrackPointDto>
    private lateinit var mapsContentAdapter : MapsViewPagerAdapter
    private lateinit var handler : Handler
    private lateinit var marker : Marker
    private var musicStatus: MusicChangedStatus = MusicChangedStatus.STOP
}