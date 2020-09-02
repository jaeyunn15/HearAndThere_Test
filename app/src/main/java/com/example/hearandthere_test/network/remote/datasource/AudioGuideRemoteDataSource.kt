package com.example.hearandthere_test.network.remote.datasource

import com.example.hearandthere_test.model.response.ResAudioGuideListDto
import com.example.hearandthere_test.model.response.ResAudioTrackInfoListDto
import io.reactivex.Single

interface AudioGuideRemoteDataSource {
    fun getGuideList(category : String) : Single<ResAudioGuideListDto>
    fun getTrackList(audioGuideId : Int) : Single<ResAudioTrackInfoListDto>
}