package com.example.hearandthere_test.network.repository

import com.example.hearandthere_test.model.response.ResAudioGuideDirectionsDto
import com.example.hearandthere_test.model.response.ResAudioGuideListDto
import com.example.hearandthere_test.model.response.ResNearestAudioTrackDto
import com.example.hearandthere_test.model.response.ResAudioTrackInfoListDto
import io.reactivex.Single

interface AudioGuideRepo {
    fun getGuideList(category : String) : Single<ResAudioGuideListDto>
    fun getTrackList(audioGuideId : Int) : Single<ResAudioTrackInfoListDto>
    fun getGuideDirections(audioGuideId : Int) : Single<ResAudioGuideDirectionsDto>
    fun getTrackListByLocation(audioGuideId : Int, userLatitude : Double, userLongitude : Double) : Single<ResNearestAudioTrackDto>
    fun insert(audioTrackInfoList : ResAudioTrackInfoListDto)
    fun delete()
    fun getOneAudioGuideByAudioGuideId(GuideID : Int) : Single<ResAudioTrackInfoListDto>

}