package com.example.hearandthere_test.network.remote.api

import com.example.hearandthere_test.model.response.ResAudioGuideListDto
import com.example.hearandthere_test.model.response.ResAudioTrackInfoListDto
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AudioGuideRepoApi {
    @GET("audio-guides")
    fun getAudioGuides(
        @Query("category")category : String
    ) : Single<ResAudioGuideListDto>

    @GET("audio-guides/{audio-guide-id}/audio-tracks")
    fun getAudioTracks(
        @Path("audio-guide-id") audio_guide_id : Int
    ) : Single<ResAudioTrackInfoListDto>

}