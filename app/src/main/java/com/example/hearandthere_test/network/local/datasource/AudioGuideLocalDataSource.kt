package com.example.hearandthere_test.network.local.datasource

import com.example.hearandthere_test.model.response.ResAudioTrackInfoListDto
import io.reactivex.Single

interface AudioGuideLocalDataSource{

    fun insert(audioTrackInfoList : ResAudioTrackInfoListDto)

    fun delete()

    fun getOneAudioGuideByAudioGuideId(GuideID : Int) : Single<ResAudioTrackInfoListDto>

}