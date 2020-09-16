package com.example.hearandthere_test.network.remote

import com.example.hearandthere_test.BuildConfig
import com.example.hearandthere_test.network.remote.ApiProvider.BASE_URL
import com.example.hearandthere_test.network.remote.api.AudioGuideRepoApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiProvider {
    private const val BASE_URL = BuildConfig.BASE_URL

    fun provideAudioGuideRepoApi() : AudioGuideRepoApi
            = retrofit.build().create(AudioGuideRepoApi::class.java)

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(provideOkHttpClient())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .addConverterFactory(GsonConverterFactory.create())


    private fun provideOkHttpClient(): OkHttpClient {
        val b = OkHttpClient.Builder()
        return b.build()
    }
}