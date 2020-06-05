package com.takari.redditpostnotifier.ui.common

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import com.takari.redditpostnotifier.data.misc.RedditApi
import com.takari.redditpostnotifier.data.misc.Repository
import com.takari.redditpostnotifier.data.post.PostData
import com.takari.redditpostnotifier.data.subreddit.SubRedditData
import com.takari.redditpostnotifier.misc.ResponseState
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleSource
import javax.inject.Inject

/*
The Fragments pretty much only rely off of the state of the local db.
 */
class SharedViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    companion object {
        const val LIMIT_REACHED = "Limit Reached"
        const val NO_CONNECTION =
            "Unable to resolve host \"www.reddit.com\": No address associated with hostname"
    }

    val currentFragment = PublishRelay.create<FragmentName>()
    val subRedditDataList = BehaviorRelay.create<List<SubRedditData>>()


    fun listenToDbPostData(): Observable<List<PostData>> =
        repository.listenToPostDataInDb()

    fun deleteDbPostData(postData: PostData): Single<Unit> =
        repository.deletePostDataInDb(postData)

    fun insertDbSubRedditDataWithCap(subRedditData: SubRedditData): Single<ResponseState<Unit>> =
        repository.getCurrentDbSubRedditData()
            .map { subDataList -> if (subDataList.size >= 12) throw Exception(LIMIT_REACHED) }
            .flatMap<ResponseState<Unit>> {
                repository.insertSubRedditDataInDb(subRedditData)
                    .map { ResponseState.Success(Unit) }
            }
            .onErrorResumeNext { e ->
                SingleSource { single -> single.onSuccess(ResponseState.Error(e.message ?: "")) }
            }


    fun getSubRedditData(name: String): Single<ResponseState<SubRedditData>> =
        repository.getSubRedditData(name)

    fun listenToDbSubRedditData(): Observable<List<SubRedditData>> =
        repository.listenToDbSubRedditData()

    fun deleteDbSubRedditData(subRedditData: SubRedditData): Single<Unit> =
        repository.deleteDbSubRedditData(subRedditData)

    fun switchContainers(fragmentName: FragmentName) {
        currentFragment.accept(fragmentName)
    }

    enum class FragmentName {
        SubRedditFragment, NewPostFragment
    }
}