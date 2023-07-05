package com.takari.redditpostnotifier.features.reddit.newPostHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.takari.redditpostnotifier.features.reddit.data.Repository
import com.takari.redditpostnotifier.features.reddit.newPost.models.PostData
import kotlinx.coroutines.launch
import javax.inject.Inject


class PostHistoryViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    val dbPostData = liveData {
        repository.listenToPostDataInDb()
            .collect { postDataList -> emit(postDataList) }
    }


    fun deleteDbPostData(postData: PostData) {
        viewModelScope.launch { repository.deletePostDataInDb(postData) }
    }

    fun deleteAllDbPostData() {
        viewModelScope.launch { repository.deleteAllDbPostData() }
    }
}