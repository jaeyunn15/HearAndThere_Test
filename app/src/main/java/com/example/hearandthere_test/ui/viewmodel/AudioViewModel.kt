package com.example.hearandthere_test.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hearandthere_test.model.response.ResNearestAudioTrackDto
import com.example.hearandthere_test.model.response.ResAudioTrackInfoListDto
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

    private val _audioResponseLiveData = MutableLiveData<ResAudioTrackInfoListDto>()
    private val _audioByLocationResponseLiveData = MutableLiveData<ResNearestAudioTrackDto>()

    val audioResponseLiveData : LiveData<ResAudioTrackInfoListDto>
        get() = _audioResponseLiveData

    val nearestAudioByLocationResponseLiveData : LiveData<ResNearestAudioTrackDto>
        get() = _audioByLocationResponseLiveData



    fun getAudioGuideByAudioGuideId(GuideId: Int){
        addDisposable(repository.getTrackList(GuideId)
            .subscribe({ it ->
                it.run {
                    _audioResponseLiveData.postValue(this)
                    Log.d("getAudioGuideByAudio",this.audioGuideTitle)
                }
            },{
                Log.d("AudioViewModel Guide", "response error message : ${it.localizedMessage}")
            })
        )
    }

    fun getAudioTrackByLocation(GuideId: Int, userLatitude : Double, userLongitude : Double){
        addDisposable(repository.getTrackListByLocation(GuideId, userLatitude, userLongitude)
            .subscribe( { it ->
                it.run {
                    _audioByLocationResponseLiveData.postValue(this)
                }
            },{
                Log.d("AudioViewModel Location", "response error message : ${it.localizedMessage}")
            })
        )
    }
}