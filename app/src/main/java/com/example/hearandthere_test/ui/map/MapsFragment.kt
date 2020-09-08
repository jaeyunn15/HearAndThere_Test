package com.example.hearandthere_test.ui.map

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
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
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginBottom
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableResource
import com.example.hearandthere_test.R
import com.example.hearandthere_test.databinding.FragmentMapsBinding
import com.example.hearandthere_test.injection.Injection
import com.example.hearandthere_test.model.response.ResAudioTrackInfoItemDto
import com.example.hearandthere_test.model.response.ResNearestAudioTrackDto
import com.example.hearandthere_test.model.response.ResNearestAudioTrackInfoDto
import com.example.hearandthere_test.service.AudioService
import com.example.hearandthere_test.ui.adapter.MapsViewPagerAdapter
import com.example.hearandthere_test.ui.mapUtil.MapPermission
import com.example.hearandthere_test.ui.mapUtil.MapState
import com.example.hearandthere_test.ui.viewmodel.AudioViewModel
import com.example.hearandthere_test.util.AudioUiHelper
import com.example.hearandthere_test.util.MusicChangedStatus
import com.example.hearandthere_test.util.MusicState
import com.example.hearandthere_test.util.MusicState.PARAM_MUSIC_LIST_BY_LOCATION
import com.example.hearandthere_test.util.MusicState.PARAM_MUSIC_LIST_BY_TRACK
import com.example.hearandthere_test.util.PlayBackState
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.MarkerIcons
import kotlinx.android.synthetic.main.fragment_maps.view.*
import kotlin.collections.ArrayList
import android.graphics.drawable.Drawable as Drawable

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
    private lateinit var trackAudioIntent : Intent
    private lateinit var nearAduioIntent : Intent
    private lateinit var trackAudioList : ArrayList<ResAudioTrackInfoItemDto>  //트랙에 따른 오디오 데이터
    private lateinit var nearAudioList : ArrayList<ResNearestAudioTrackInfoDto> //내 위치에 따른 오디오 데이터
    private lateinit var mapsContentAdapter : MapsViewPagerAdapter
    private lateinit var marker : Marker
    private lateinit var handler : Handler
    private lateinit var audioUiHelper: AudioUiHelper
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

            getNearestAudioData(coord)
            observeNearestData()

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

    @SuppressLint("ResourceAsColor")
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
        trackAudioList = ArrayList()
        nearAudioList = arrayListOf()
        trackAudioIntent = Intent(context, AudioService::class.java)
        nearAduioIntent = Intent(context, AudioService::class.java)
        audioUiHelper = AudioUiHelper()
        sb_audioPlay.max = 100
    }

    private fun dataSetting(){
        audioViewModel = Injection.provideAudioViewModel()
        getData() //데이터 먼저 가져와야지
        observeData() //가져온 데이터 설정으로 넣어놔야
    }

    private fun getData(){
        audioViewModel.getAudioGuideByAudioGuideId(9)
    }

    private fun observeData(){
        val compositePageTransformer = CompositePageTransformer()
        val marginPageTransformer = MarginPageTransformer(40)
        compositePageTransformer.addTransformer(marginPageTransformer)

        audioViewModel.audioResponseLiveData.observe(this, Observer { it ->

            mapsContentAdapter = MapsViewPagerAdapter(this, it.audioTrackInfoList)

            it.run {
                this.audioTrackInfoList.forEach { audio ->
                    trackAudioList.add(audio)
                }
                putDataToService() //시작과 동시에 9번 트랙에 대한 오디오 정보를 넘김.
                drawMarker()
            }

            fragmentView.vp_mapfragment_audioInfo.let { vp ->
                vp.adapter = mapsContentAdapter
                vp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                vp.offscreenPageLimit = 3
                vp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                vp.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                vp.setPageTransformer(compositePageTransformer)
                vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        trackAudioList.forEach {
                            if (it.trackOrderNumber == position+1){
                                val movedMarkPos = LatLng(it.trackLatitude, it.trackLongitude)
                                map.moveCamera(CameraUpdate.scrollTo(movedMarkPos))
                            }
                        }
                    }
                })
            }
        })
    }

    private fun attachOnClick(){
        view_nowplay.setOnClickListener {
            if (vp.isShown){ vp.visibility = View.GONE
            }else{ vp.visibility = View.VISIBLE }
        }
        btn_PausePlay.setOnClickListener {
            if (musicStatus == MusicChangedStatus.STOP){ play() }
            else if (musicStatus == MusicChangedStatus.PLAY){ pause() }
            else if (musicStatus == MusicChangedStatus.PAUSE){ play() }
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
            context!!.registerReceiver(MusicReceiver(), intentFilter) //정적으로 리시버 등록
        }
    }

    private fun tryEnableLocation() {
        if (MapPermission.PERMISSIONS.all { ContextCompat.checkSelfPermission(requireContext(), it) == PermissionChecker.PERMISSION_GRANTED }) {
            enableLocation()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                MapPermission.PERMISSIONS,
                MapState.PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getNearestAudioData(loc : LatLng){
        audioViewModel.getAudioTrackByLocation(9,loc.latitude,loc.longitude)
    }

    private fun observeNearestData(){
        audioViewModel.nearestAudioByLocationResponseLiveData.observe(this, Observer{
            if (it.isAudioTrackNearBy){ //50m 내에 있다면?
                var newlist : MutableList<ResNearestAudioTrackInfoDto> = ArrayList()
                nearAudioList.add(it.nearestTrackInfo)
                newlist = nearAudioList
                newlist = newlist.toSet().toMutableList()
                nearAudioList = newlist as ArrayList<ResNearestAudioTrackInfoDto>
                Log.d("OND TEST", "${nearAudioList.size}")
                nearAduioIntent.putParcelableArrayListExtra(PARAM_MUSIC_LIST_BY_LOCATION, nearAudioList)
                MapState.nearAudioGuideEnabled = true
                putDataToService() //음악 데이터 보냄.

            } else { //50m 내에 있지 않다면?
                MapState.nearAudioGuideEnabled = false
                putDataToService() //음악 데이터 보냄.
                Log.d("OND TEST","Near Audio Guide isn't Exist!!")
            }
        })
    }

    private fun putDataToService(){
        trackAudioIntent.putParcelableArrayListExtra(PARAM_MUSIC_LIST_BY_TRACK, trackAudioList)
        if (!MapState.locationEnabled){ //위치 추적 꺼져있다면 무조건 트랙 노래를 서비스에 보냄.
            context?.startService(trackAudioIntent)
        } else{ //위치 추적 켜져있다면
            if (chkIsPlaying()){ //재생중인 음악 있다면
                if (MapState.nearAudioGuideEnabled){ //주변에 오디오가이드 있다면?
                    stop() //일단 현재 음악 끄고
                    context?.stopService(trackAudioIntent)
                    context?.startService(nearAduioIntent) //데이터 보내서 다시 키자.
                    //음악 다시 트는 코드
                }
            }else{ //재생중인 음악 없다면
                if (MapState.nearAudioGuideEnabled){ //주변에 오디오가이드 있다면?
                    context?.startService(nearAduioIntent)
                }
            }
        }
    }

    private fun drawMarker(){
        trackAudioList.forEach {item ->
            marker = Marker()
            marker.let {
                it.position = LatLng(item.trackLatitude, item.trackLongitude)
                val name = "marker_${item.trackOrderNumber}"
                Log.d("TON ", name)
                val drawRes = this.resources.getIdentifier(name, "drawable", requireContext().packageName)
                it.icon = OverlayImage.fromResource(drawRes)
                it.tag = item.trackOrderNumber
                it.onClickListener(this, marker.position)
                it.map = map
            }
        }
    }


    private operator fun Overlay.OnClickListener?.invoke(mapsFragment: MapsFragment, markerPosition : LatLng) {
        mapsFragment.marker.setOnClickListener {
            //map.moveCamera(CameraUpdate.scrollTo(markerPosition))
            if (!vp.isShown){ vp.visibility = View.VISIBLE }
            vp.currentItem = (it.tag) as Int -1
            return@setOnClickListener true
        }
    }

    fun clickListener(position : Int){
        Log.d("PLAY AUDIO ", "$position Clicked!!")
        view_nowplay.visibility = View.VISIBLE
        val mLayoutParams : CoordinatorLayout.LayoutParams = vp.layoutParams as CoordinatorLayout.LayoutParams
        mLayoutParams.bottomMargin = 150
        vp.layoutParams = mLayoutParams
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == MapState.PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PermissionChecker.PERMISSION_GRANTED }) {
                enableLocation()
            } else {
                fab.setImageResource(R.drawable.ic_my_loc)
            }
            return
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        if (MapState.trackingEnabled) { enableLocation() }
    }

    override fun onStop() {
        super.onStop()
        disableLocation()
    }

    @SuppressLint("ResourceAsColor")
    override fun onMapReady(naverMap: NaverMap) {
        map = naverMap
        drawPolyLine()

        fab.setOnClickListener {
            if (MapState.trackingEnabled){
                disableLocation()
                fab.setImageResource(R.drawable.ic_my_loc)
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

        map.setOnMapClickListener { pointF, latLng ->
            if (vp.isShown){ vp.visibility = View.GONE }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun drawPolyLine(){
        val polyline = PolylineOverlay()
        //여기에 map view model에서 옵저빙해온 Arraylist값에서 polyline값들을 빼서 polylist에 add하여 이 부분에 넣고 그린다.
        // polyline draw 와 location marker는 별개로 동작.
        // 마커와 뷰페이저 같이 동작.
        polyline.coords = listOf(
            LatLng(37.359924641705476, 127.1148204803467),
            LatLng(37.36343797188166, 127.11486339569092),
            LatLng(37.368520071054576, 127.11473464965819),
            LatLng(37.3685882848096, 127.1088123321533),
            LatLng(37.37295383612657, 127.10876941680907),
            LatLng(37.38001321351567, 127.11851119995116),
            LatLng(37.378546827477855, 127.11984157562254),
            LatLng(37.376637072444105, 127.12052822113036),
            LatLng(37.37530703574853, 127.12190151214598),
            LatLng(37.371657839593894, 127.11645126342773),
            LatLng(37.36855417793982, 127.1207857131958)
        )
        polyline.color = R.color.colorAccent
        polyline.map = map
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
        LocationServices.getFusedLocationProviderClient(requireContext()).removeLocationUpdates(
            locationCallback
        )
        MapState.locationEnabled = false
        updateFAB()
    }

    private fun updateFAB(){
        if (MapState.trackingEnabled){
            fab.setImageResource(R.drawable.ic_my_loc)
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

    private fun stop() {
        optMusic(MusicState.ACTION_MUSIC_STOP)
        handler.removeCallbacks(updater)
        musicStatus = MusicChangedStatus.STOP
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

    private fun updateSeekBar(){
        sb_audioPlay.progress = ( (musicPlayer?.currentPosition!! * 100) / musicPlayer?.duration!!)
        handler.postDelayed(updater, 1000)
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

    private fun updateTitle(trackTitle: String, audioTitle: String){
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