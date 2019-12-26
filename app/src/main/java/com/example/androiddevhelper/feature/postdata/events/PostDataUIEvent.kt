package com.example.androiddevhelper.feature.postdata.events

import com.example.androiddevhelper.feature.postdata.data.local.PostData

sealed class PostDataUIEvent {
    object OnCreateFinish: PostDataUIEvent()
    data class AdapterItemClick(val postData: PostData) : PostDataUIEvent()
    data class AdapterSwipe(val postData: PostData) : PostDataUIEvent()
    data class ValidateSubClick(val subReddit: String): PostDataUIEvent()
    object ListenButtonClick: PostDataUIEvent()
    object StopListeningButtonClick: PostDataUIEvent()
}