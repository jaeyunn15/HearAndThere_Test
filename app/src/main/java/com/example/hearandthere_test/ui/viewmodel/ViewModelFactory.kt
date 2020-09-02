package com.example.hearandthere_test.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hearandthere_test.network.repository.AudioGuideRepo


class ViewModelFactory(
    private val audioRepository: AudioGuideRepo
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AudioViewModel::class.java)) {
            AudioViewModel(audioRepository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}