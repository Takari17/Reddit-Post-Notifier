package com.takari.redditpostnotifier.features.reddit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.takari.redditpostnotifier.features.reddit.data.Repository
import com.takari.redditpostnotifier.features.reddit.newPost.models.PostData
import com.takari.redditpostnotifier.features.reddit.subreddit.models.SubRedditData
import com.takari.redditpostnotifier.utils.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject


//The Views pretty much only rely off of the state of the local db.
@HiltViewModel
class SharedViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    companion object {
        const val LIMIT_REACHED = "Limit Reached" //todo string resource
        const val NO_CONNECTION =  //todo string resource
            "Unable to resolve host \"www.reddit.com\": No address associated with hostname"
    }

    var subRedditDataList: List<SubRedditData>? = null

    val dbPostData = liveData {
        repository.listenToPostDataInDb()
            .collect { postDataList -> emit(postDataList) }
    }

    val dbSubRedditData = liveData {
        repository.listenToDbSubRedditData()
            .collect { subDataList -> emit(subDataList) }
    }

    fun deleteDbPostData(postData: PostData) {
        viewModelScope.launch { repository.deletePostDataInDb(postData) }
    }

    fun getAndCacheSubRedditData(subName: String): Flow<ResponseState<Unit>> = flow {
        val subRedditData = repository.getSubRedditData(subName)

        val currentSubRedditSize = repository.getCurrentDbSubRedditData().size

        if (currentSubRedditSize >= 12) {
            emit(ResponseState.Error(Exception(LIMIT_REACHED)))
        } else {
            repository.insertSubRedditDataInDb(subRedditData)
            emit(ResponseState.Success(Unit))
        }

    }.catch { e -> emit(ResponseState.Error(e)) }

    fun deleteDbSubRedditData(subRedditData: SubRedditData) {
        viewModelScope.launch { repository.deleteDbSubRedditData(subRedditData) }
    }
}