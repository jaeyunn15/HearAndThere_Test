package com.example.hearandthere_test.model.response

import com.example.hearandthere_test.model.response.ResAudioGuideItemDto

data class ResAudioGuideListDto (
    val audioGuideList : ArrayList<ResAudioGuideItemDto>,
    val category : String
)