package com.example.hearandthere_test.network.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hearandthere_test.model.response.ResAudioTrackInfoListDto
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.internal.operators.completable.CompletableAmb
import retrofit2.Response

@Dao
interface AudioGuideDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(audioTrackInfoListDto: ResAudioTrackInfoListDto)

    @Query("DELETE FROM AudioTrackInfoList")
    fun delete()

    @Query("SELECT * FROM AudioTrackInfoList WHERE audioGuideId = :GuideId")
    fun getAudioGuideByAudioGuideId(GuideId : Int) : Single<ResAudioTrackInfoListDto>

}