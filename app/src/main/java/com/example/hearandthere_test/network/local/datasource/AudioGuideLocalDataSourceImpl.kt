package com.example.hearandthere_test.network.local.datasource


import com.example.hearandthere_test.model.response.ResAudioTrackInfoListDto
import com.example.hearandthere_test.network.local.LocalDataSource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AudioGuideLocalDataSourceImpl(private val repo : LocalDataSource) :
    AudioGuideLocalDataSource {

    override fun insert(audioTrackInfoList: ResAudioTrackInfoListDto){
        repo.insert(audioTrackInfoList)
    }

    override fun delete(){
        repo.delete()
    }

    override fun getOneAudioGuideByAudioGuideId(GuideID: Int): Single<ResAudioTrackInfoListDto> {
        return repo.getOneAudioGuideByAudioGuideId(GuideID)
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
    }

}