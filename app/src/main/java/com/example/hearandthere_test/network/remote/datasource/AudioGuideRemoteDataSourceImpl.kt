package com.example.hearandthere_test.network.remote.datasource

import com.example.hearandthere_test.model.response.ResAudioGuideListDto
import com.example.hearandthere_test.model.response.ResAudioTrackInfoListDto
import com.example.hearandthere_test.network.remote.api.AudioGuideRepoApi
import com.example.hearandthere_test.network.remote.datasource.AudioGuideRemoteDataSource

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AudioGuideRemoteDataSourceImpl(private val AudioGuideRepoApi : AudioGuideRepoApi) :
    AudioGuideRemoteDataSource {
    override fun getGuideList(category: String): Single<ResAudioGuideListDto> {
        return AudioGuideRepoApi.getAudioGuides(category = category)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getTrackList(audioGuideId: Int): Single<ResAudioTrackInfoListDto> {
        return AudioGuideRepoApi.getAudioTracks(audio_guide_id = audioGuideId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}