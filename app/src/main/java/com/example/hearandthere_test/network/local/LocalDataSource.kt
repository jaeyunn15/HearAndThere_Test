package com.example.hearandthere_test.network.local

import com.example.hearandthere_test.model.response.ResAudioTrackInfoListDto
import io.reactivex.Single

class LocalDataSource(private val audioGuideDao : AudioGuideDao) {

    fun insert(audioTrackInfoList : ResAudioTrackInfoListDto) {
        return audioGuideDao.insert(audioTrackInfoList)
    }

    fun delete() {
        return audioGuideDao.delete()
    }

    fun getOneAudioGuideByAudioGuideId(GuideID : Int) : Single<ResAudioTrackInfoListDto>{
        return audioGuideDao.getAudioGuideByAudioGuideId(GuideID)
    }
}

