package com.example.hearandthere_test.network.repository

import com.example.hearandthere_test.model.response.ResAudioGuideListDto
import com.example.hearandthere_test.model.response.ResNearestAudioTrackDto
import com.example.hearandthere_test.model.response.ResAudioTrackInfoListDto
import com.example.hearandthere_test.network.local.datasource.AudioGuideLocalDataSource
import com.example.hearandthere_test.network.remote.ApiProvider
import com.example.hearandthere_test.network.remote.datasource.AudioGuideRemoteDataSource
import com.example.hearandthere_test.network.remote.datasource.AudioGuideRemoteDataSourceImpl

import io.reactivex.Single

class AudioGuideRepoImpl(private val local : AudioGuideLocalDataSource) : AudioGuideRepo {
    private val remote = ApiProvider.provideAudioGuideRepoApi()
    private val remoteDataSource : AudioGuideRemoteDataSource = AudioGuideRemoteDataSourceImpl(remote)

    override fun getGuideList(category: String): Single<ResAudioGuideListDto>
        = remoteDataSource.getGuideList(category)

    override fun getTrackList(audioGuideId: Int): Single<ResAudioTrackInfoListDto>
        = remoteDataSource.getTrackList(audioGuideId)

    override fun getTrackListByLocation(
        audioGuideId: Int,
        userLatitude: Double,
        userLongitude: Double
    ): Single<ResNearestAudioTrackDto>
            =remoteDataSource.getTrackListByLocation(audioGuideId, userLatitude, userLongitude)


    override fun insert(audioTrackInfoList: ResAudioTrackInfoListDto)
        = local.insert(audioTrackInfoList)

    override fun delete()
        = local.delete()

    override fun getOneAudioGuideByAudioGuideId(GuideID: Int): Single<ResAudioTrackInfoListDto>
        = local.getOneAudioGuideByAudioGuideId(GuideID)
}