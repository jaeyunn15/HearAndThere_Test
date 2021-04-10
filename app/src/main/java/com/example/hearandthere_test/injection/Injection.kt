package com.example.hearandthere_test.injection

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.hearandthere_test.MyApplication
import com.example.hearandthere_test.network.local.AudioGuideDatabase
import com.example.hearandthere_test.network.local.LocalDataSource
import com.example.hearandthere_test.network.local.datasource.AudioGuideLocalDataSource
import com.example.hearandthere_test.network.local.datasource.AudioGuideLocalDataSourceImpl
import com.example.hearandthere_test.network.repository.AudioGuideRepo
import com.example.hearandthere_test.network.repository.AudioGuideRepoImpl
import com.example.hearandthere_test.ui.viewmodel.AudioViewModel
import com.example.hearandthere_test.ui.viewmodel.ViewModelFactory

object Injection {

    private val context = MyApplication.context

    fun provideAudioViewModel() : AudioViewModel {
        val dao = AudioGuideDatabase.getInstance(context!!).audioGuideDao()
        val local = LocalDataSource(dao)
        val audioDataSource : AudioGuideLocalDataSource = AudioGuideLocalDataSourceImpl(local)
        val repository : AudioGuideRepo = AudioGuideRepoImpl(audioDataSource)
        return ViewModelProvider(ViewModelStore(), ViewModelFactory(repository)).get(AudioViewModel::class.java)
    }
}