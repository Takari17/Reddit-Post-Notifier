package com.example.androiddevhelper.feature.postdata.events

import com.example.androiddevhelper.feature.postdata.ui.PostDataUIState
import io.reactivex.Observable

interface PostDataEvents {

    fun onViewEvent(event: PostDataUIEvent)

    fun state(): Observable<PostDataUIState>

    fun singleEvent(): Observable<PostDataSingleEvent>
}