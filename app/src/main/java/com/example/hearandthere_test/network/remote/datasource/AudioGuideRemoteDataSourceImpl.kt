package com.example.hearandthere_test.network.remote.datasource

import com.example.hearandthere_test.model.response.*
import com.example.hearandthere_test.network.remote.api.AudioGuideRepoApi

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

    override fun getGuideDirections(audioGuideId: Int): Single<ResAudioGuideDirectionsDto> {
        return AudioGuideRepoApi.getAudioDirections(audio_guide_id = audioGuideId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getTrackListByLocation(
        audioGuideId: Int,
        user_latitude: Double,
        user_longitude: Double
    ): Single<ResNearestAudioTrackDto> {
        return AudioGuideRepoApi.getAudioTracksByLocation(
            audio_guide_id = audioGuideId,
            user_latitude = user_latitude,
            user_longitude = user_longitude
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getDetailAudioGuide(
        audioGuideId: Int,
        lan: String
    ): Single<ResSingleAudioGuideDetailDto> {
        return AudioGuideRepoApi.getDetailAudioGuides(audioGuideId, lan)
    }

    override fun getDetailAudioGuidePolyline(audioGuideId: Int): Single<ResAudioGuideDirectionsDto> {
        return AudioGuideRepoApi.getDetailAudioGuidePolyline(audioGuideId)
    }
}