package com.example.androiddevhelper.feature

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.androiddevhelper.feature.postdata.data.local.PostData
import com.example.androiddevhelper.feature.postdata.data.local.PostDataDao
import com.example.androiddevhelper.feature.postdata.data.remote.RedditApi
import com.example.androiddevhelper.feature.postdata.data.remote.RedditResponse
import com.example.androiddevhelper.feature.postdata.service.PostDataService
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Repository @Inject constructor(
    private val redditApi: RedditApi,
    private val postDataDao: PostDataDao,
    private val sharedPrefs: SharedPreferences
) {

    //The PostDataService's means of telling the UI to reset it's state (observed by the view model).
    val reset = PublishRelay.create<Unit>()


    fun insertPostData(post: PostData): Completable =
        postDataDao.insert(post)


    fun listenToPostData(): Observable<List<PostData>> =
        postDataDao.listen()


    fun deletePostData(postData: PostData): Completable =
        postDataDao.deleteItem(postData)


    fun deleteAllPostData(): Completable =
        postDataDao.deleteAll()


    fun isServiceRunning() = PostDataService.getIsRunning()


    fun validateSubReddit(name: String) =
        redditApi.validateSubReddit(name)
            .map {
                if (it.data.children.isEmpty()) throw Exception("Invalid SubReddit")
                else it
            }


    /**
     * Returns the first 24 new post.
     */
    fun executeGetNewPost(subReddit: String): Observable<RedditResponse> =
        redditApi.getAllPostData(subReddit)


    fun saveSubRedditName(subReddit: String) {
        sharedPrefs.edit { putString("name", subReddit) }
    }


    fun getSavedSubRedditName(defaultValue: String): String =
        sharedPrefs.getString("name", defaultValue)!!
}
