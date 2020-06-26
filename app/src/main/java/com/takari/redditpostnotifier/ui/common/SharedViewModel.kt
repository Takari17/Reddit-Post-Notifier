package com.takari.redditpostnotifier.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.takari.redditpostnotifier.data.misc.Repository
import com.takari.redditpostnotifier.data.post.PostData
import com.takari.redditpostnotifier.data.subreddit.SubRedditData
import com.takari.redditpostnotifier.misc.ResponseState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject


//The Views pretty much only rely off of the state of the local db.
class SharedViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    companion object {
        const val LIMIT_REACHED = "Limit Reached"
        const val NO_CONNECTION = "Unable to resolve host \"www.reddit.com\": No address associated with hostname"
    }

    var switchContainers: (FragmentName) -> Unit = {}
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
        insertDbSubRedditDataWithCap(repository.getSubRedditData(subName))
        emit(ResponseState.Success(Unit))
    }.catch<ResponseState<Unit>> { e -> emit(ResponseState.Error(e)) }

    fun insertDbSubRedditDataWithCap(subRedditData: SubRedditData) {
        viewModelScope.launch {
            val currentSubData = repository.getCurrentDbSubRedditData()
            if (currentSubData.size >= 12) throw Exception(LIMIT_REACHED)
            else repository.insertSubRedditDataInDb(subRedditData)
        }
    }

    fun deleteDbSubRedditData(subRedditData: SubRedditData) {
        viewModelScope.launch { repository.deleteDbSubRedditData(subRedditData) }
    }

    enum class FragmentName {
        SubRedditFragment, NewPostFragment
    }
}