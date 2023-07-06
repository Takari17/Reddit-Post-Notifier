package com.takari.redditpostnotifier.features.reddit.newPostHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.takari.redditpostnotifier.features.reddit.data.RedditRepository
import com.takari.redditpostnotifier.features.reddit.newPost.models.PostData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PostHistoryViewModel @Inject constructor(private val repository: RedditRepository) : ViewModel() {

    val dbPostData = liveData {
        repository.observePostData()
            .collect { postDataList -> emit(postDataList) }
    }

    fun deleteDbPostData(postData: PostData) {
        viewModelScope.launch { repository.deletePostDataInDb(postData) }
    }

    fun deleteAllDbPostData() {
        viewModelScope.launch { repository.deleteAllDbPostData() }
    }
}