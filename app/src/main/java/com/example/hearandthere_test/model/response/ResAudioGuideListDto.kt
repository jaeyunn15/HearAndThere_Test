package com.example.hearandthere_test.model.response

data class ResAudioGuideListDto (
    val audioGuideList : ArrayList<ResAudioGuideItemDto>,
    val category : String
)