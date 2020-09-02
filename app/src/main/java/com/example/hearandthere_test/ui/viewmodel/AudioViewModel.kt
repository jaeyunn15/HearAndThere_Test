package com.example.hearandthere_test.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
    val audioResponseLiveData : LiveData<ResAudioTrackInfoListDto>
        get() = _audioResponseLiveData

    fun getAudioGuideByAudioGuideId(GuideId: Int){
        Log.d("getAudioGuideByAudio","진입")

        addDisposable(repository.getTrackList(GuideId)
            .subscribe({ it ->
                it.run {
                    _audioResponseLiveData.postValue(this)
                    Log.d("getAudioGuideByAudio",this.audioGuideTitle)
                }
            },{
                Log.d("AudioViewModel ", "response error message : ${it.localizedMessage}")
            })
        )
    }
}