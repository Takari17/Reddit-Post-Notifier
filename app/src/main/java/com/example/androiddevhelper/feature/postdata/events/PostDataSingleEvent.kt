package com.example.androiddevhelper.feature.postdata.events

sealed class PostDataSingleEvent {
    object StartValidAnimation : PostDataSingleEvent()
    object StartListeningAnimation : PostDataSingleEvent()
    object ResetAnimations : PostDataSingleEvent()
    object RestoreAnimation : PostDataSingleEvent()
    object StartService : PostDataSingleEvent()
    object ResetService : PostDataSingleEvent()
    data class OpenRedditPost(val sourceUrl: String) : PostDataSingleEvent()
}
