package com.takari.redditpostnotifier.data.misc

import android.content.SharedPreferences
import androidx.core.content.edit
import com.takari.redditpostnotifier.data.post.NewPostListResponse
import com.takari.redditpostnotifier.data.post.Post
import com.takari.redditpostnotifier.data.post.PostData
import com.takari.redditpostnotifier.data.post.PostDataDao
import com.takari.redditpostnotifier.data.subreddit.SubRedditData
import com.takari.redditpostnotifier.data.subreddit.SubRedditDataDao
import com.takari.redditpostnotifier.misc.ResponseState
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleSource
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/*
Remember, the repo should be the only one making API calls, it's the single source of truth.
 */
@Singleton
class Repository @Inject constructor(
    private val redditApi: RedditApi,
    private val postDataDao: PostDataDao,
    private val subRedditDataDao: SubRedditDataDao,
    private val sharedPrefs: SharedPreferences
) {

    private fun getPostDataList(name: String): Single<NewPostListResponse> =
        redditApi.getNewPostList(name)

    fun getMultiplePostDataList(subRedditDataList: List<SubRedditData>): Observable<List<Post>> =
        Observable.just(Unit)
            .flatMapIterable { subRedditDataList }
            .flatMapSingle { subRedditData -> getPostDataList(subRedditData.name) }
            .map { it.data.children }

    fun getNewPostDataWithInterval(
        millis: Long,
        subRedditDataList: List<SubRedditData>,
        viewedPost: MutableList<PostData>
    ): Observable<PostData> =
        Observable.interval(millis, TimeUnit.MILLISECONDS)
            .flatMap { getMultiplePostDataList(subRedditDataList) }
            .flatMap { postList ->
                Observable.fromIterable(postList)
                    .filter { post -> post.data !in viewedPost }
                    .map { post ->
                        viewedPost.add(post.data)
                        post.data
                    }
            }

    fun getSubRedditData(name: String): Single<ResponseState<SubRedditData>> {

        return redditApi.getSubRedditData(name)
            .map<ResponseState<SubRedditData>> { subRedditData ->
                ResponseState.Success(subRedditData.data)
            }
            .onErrorResumeNext { e ->
                SingleSource { single -> single.onSuccess(ResponseState.Error(e.message ?: "")) }
            }
    }

    fun insertPostDataInDb(post: PostData): Single<Unit> =
        postDataDao.insertReplace(post)

    fun listenToPostDataInDb(): Observable<List<PostData>> =
        postDataDao.listenForPostData()

    fun deletePostDataInDb(postData: PostData): Single<Unit> =
        postDataDao.deleteItem(postData)

    fun deleteAllDbPostData(): Single<Unit> =
        postDataDao.deleteAll()

    fun insertSubRedditDataInDb(subRedditData: SubRedditData) =
        subRedditDataDao.insertReplace(subRedditData)

    fun listenToDbSubRedditData(): Observable<List<SubRedditData>> =
        subRedditDataDao.listenToSubRedditData()

    fun getCurrentDbSubRedditData(): Single<List<SubRedditData>> =
        subRedditDataDao.getCurrentSubRedditData()

    fun deleteDbSubRedditData(subRedditData: SubRedditData): Single<Unit> =
        subRedditDataDao.deleteItem(subRedditData)

    fun getIntFromSharedPrefs(key: String, defaultValue: Int): Int =
        sharedPrefs.getInt(key, defaultValue)

    fun saveIntToSharedPrefs(key: String, value: Int) {
        sharedPrefs.edit { putInt(key, value) }
    }
}