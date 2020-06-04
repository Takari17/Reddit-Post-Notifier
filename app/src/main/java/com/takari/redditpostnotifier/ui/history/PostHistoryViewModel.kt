package com.takari.redditpostnotifier.ui.history

import androidx.lifecycle.ViewModel
import com.takari.redditpostnotifier.data.misc.Repository
import com.takari.redditpostnotifier.data.post.PostData
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject


class PostHistoryViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    fun listenToDbPostData(): Observable<List<PostData>> =
        repository.listenToPostDataInDb()

    fun deleteDbPostData(postData: PostData): Single<Unit> =
        repository.deletePostDataInDb(postData)

    fun deleteAllDbPostData(): Single<Unit> =
        repository.deleteAllDbPostData()
}