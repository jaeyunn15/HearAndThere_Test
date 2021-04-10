package com.example.hearandthere_test.network.repository

import com.example.hearandthere_test.model.response.*
import io.reactivex.Single

interface AudioGuideRepo {
    fun getGuideList(category : String) : Single<ResAudioGuideListDto>
    fun getTrackList(audioGuideId : Int) : Single<ResAudioTrackInfoListDto>
    fun getGuideDirections(audioGuideId : Int) : Single<ResAudioGuideDirectionsDto>
    fun getTrackListByLocation(audioGuideId : Int, userLatitude : Double, userLongitude : Double) : Single<ResNearestAudioTrackDto>
    fun insert(audioTrackInfoList : ResAudioTrackInfoListDto)
    fun delete()
    fun getOneAudioGuideByAudioGuideId(GuideID : Int) : Single<ResAudioTrackInfoListDto>

    fun getDetailAudioGuide(audioGuideId: Int, lan : String) : Single<ResSingleAudioGuideDetailDto>
    fun getDetailAudioGuidePolyline(audioGuideId: Int) : Single<ResAudioGuideDirectionsDto>
}