package com.example.hearandthere_test.network.remote

import com.example.hearandthere_test.network.remote.api.AudioGuideRepoApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiProvider {
    private const val BASE_URL = "http://ec2-15-164-77-34.ap-northeast-2.compute.amazonaws.com:8080/"


    fun provideAudioGuideRepoApi() : AudioGuideRepoApi = retrofit.build().create(AudioGuideRepoApi::class.java)


    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(provideOkHttpClient())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .addConverterFactory(GsonConverterFactory.create())


    // 네트뭐크 통신에 사용할 클라이언트 객체를 생성합니다.
    private fun provideOkHttpClient(): OkHttpClient {
        val b = OkHttpClient.Builder()
        // 이 클라이언트를 통해 오고 가는 네트워크 요청/응답을 로그로 표시하도록 합니다.
        return b.build()
    }
}