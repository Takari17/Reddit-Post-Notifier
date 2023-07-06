package com.takari.redditpostnotifier.features.reddit.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.takari.redditpostnotifier.features.reddit.newPost.models.Post
import com.takari.redditpostnotifier.features.reddit.newPost.models.PostData
import com.takari.redditpostnotifier.features.reddit.newPost.models.PostDataDao
import com.takari.redditpostnotifier.features.reddit.subreddit.models.SubRedditData
import com.takari.redditpostnotifier.features.reddit.subreddit.models.SubRedditDataDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


/**
 * This class exposes methods for retrieving and storing data from the reddit API. This repository
 * is shared across multiple modules because the data within the modules are linked to each other.
 * For example, the newPost module depends on the subreddits selected in the subreddit module.
 */
@Singleton
class RedditRepository @Inject constructor(
    private val redditApi: RedditApi,
    private val postDataDao: PostDataDao,
    private val subRedditDataDao: SubRedditDataDao,
    private val sharedPrefs: SharedPreferences
) {

    /**
     * Iterates through a list of subreddits and retrieves the newest post for each of them.
     */
    suspend fun getNewPostList(vararg subName: String): Flow<List<Post>> = flow {
        subName.forEach { name ->
            val postList = redditApi.getNewPostList(name).data.children
            emit(postList)
        }
    }

    suspend fun getSubRedditData(name: String): SubRedditData {
        return redditApi.getSubRedditData(name).data
    }

    suspend fun insertPostData(post: PostData) = withContext(Dispatchers.Default) {
        postDataDao.insertReplace(post)
    }

    /**
     * Listens for changes to PostData in the local Room DB.
     */
    fun observePostData(): Flow<List<PostData>> {
        return postDataDao.listenForPostData()
    }

    suspend fun deletePostDataInDb(postData: PostData) {
        return postDataDao.deleteItem(postData)
    }

    suspend fun deleteAllDbPostData() {
        postDataDao.deleteAll()
    }

    suspend fun insertSubRedditDataInDb(subRedditData: SubRedditData) {
        subRedditDataDao.insertReplace(subRedditData)
    }

    fun listenToDbSubRedditData(): Flow<List<SubRedditData>> {
        return subRedditDataDao.listenToSubRedditData()
    }

    suspend fun getCurrentDbSubRedditData(): List<SubRedditData> {
        return subRedditDataDao.getCurrentSubRedditData()
    }

    suspend fun deleteDbSubRedditData(subRedditData: SubRedditData): Unit {
        subRedditDataDao.deleteItem(subRedditData)
    }

    fun getApiRequestRate(): Int = sharedPrefs.getInt("apiRequestRate", 1)

    fun saveApiRequestRate(value: Int) {
        sharedPrefs.edit { putInt("apiRequestRate", value) }
    }
}