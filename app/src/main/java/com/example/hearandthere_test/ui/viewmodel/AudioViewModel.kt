package com.example.hearandthere_test.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hearandthere_test.model.response.ResAudioGuideDirectionsDto
import com.example.hearandthere_test.model.response.ResSingleAudioGuideDetailDto
import com.example.hearandthere_test.network.repository.AudioGuideRepo
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class AudioViewModel (
    private val repository : AudioGuideRepo
) : ViewModel(){

    private val compositeDisposable : CompositeDisposable = CompositeDisposable()
    private fun addDisposable(disposable : Disposable){
        compositeDisposable.add(disposable)
    }

    private val _audioResponseLiveData = MutableLiveData<ResSingleAudioGuideDetailDto>()
    private val _audioTrackDirectionsLiveData = MutableLiveData<ResAudioGuideDirectionsDto>()

    val audioResponseLiveData : LiveData<ResSingleAudioGuideDetailDto>
        get() = _audioResponseLiveData

    val audioTrackDirectionsLiveData : LiveData<ResAudioGuideDirectionsDto>
        get() = _audioTrackDirectionsLiveData

    init {
        getAudioGuideByAudioGuideId(7)
        getTrackDirections(7)
    }

    fun getAudioGuideByAudioGuideId(GuideId: Int){
        addDisposable(repository.getDetailAudioGuide(GuideId, "eng")
            .subscribe({ it ->
                it.run {
                    _audioResponseLiveData.postValue(this)
                    this.tracksList?.forEach {ra ->
                        Log.d("getAudioGuideByAudio : ", ra.title)
                    }
                }
            },{
                Log.d("AudioViewModel Guide", "response error message : ${it.localizedMessage}")
            })
        )
    }

    fun getTrackDirections(GuideId: Int){
        addDisposable(repository.getDetailAudioGuidePolyline(GuideId)
            .subscribe( { it ->
                it.run {
                    _audioTrackDirectionsLiveData.postValue(this)
                    it.trackPoints.forEach {
                        Log.d("AudioViewModel Directs", it.trackLatitude.toString())
                    }
                }
            },{
                Log.d("AudioViewModel Directs", "response error message : ${it.localizedMessage}")
            })
        )
    }
}