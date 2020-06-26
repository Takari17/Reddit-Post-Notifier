package com.takari.redditpostnotifier.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.takari.redditpostnotifier.data.misc.Repository
import com.takari.redditpostnotifier.data.post.PostData
import com.takari.redditpostnotifier.misc.ResponseState
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
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