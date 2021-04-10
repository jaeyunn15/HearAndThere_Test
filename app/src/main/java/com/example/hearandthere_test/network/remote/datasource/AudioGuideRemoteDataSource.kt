package com.example.hearandthere_test.network.remote.datasource

import com.example.hearandthere_test.model.response.*
import io.reactivex.Single

interface AudioGuideRemoteDataSource {
    fun getGuideList(category : String) : Single<ResAudioGuideListDto>
    fun getTrackList(audioGuideId : Int) : Single<ResAudioTrackInfoListDto>
    fun getGuideDirections(audioGuideId : Int) : Single<ResAudioGuideDirectionsDto>
    fun getTrackListByLocation(audioGuideId : Int, user_latitude:Double, user_longitude:Double) : Single<ResNearestAudioTrackDto>

    fun getDetailAudioGuide(audioGuideId: Int, lan : String) : Single<ResSingleAudioGuideDetailDto>
    fun getDetailAudioGuidePolyline(audioGuideId: Int) : Single<ResAudioGuideDirectionsDto>
}