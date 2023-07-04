package com.takari.redditpostnotifier.data.misc

import android.content.SharedPreferences
import androidx.core.content.edit
import com.takari.redditpostnotifier.data.post.Post
import com.takari.redditpostnotifier.data.post.PostData
import com.takari.redditpostnotifier.data.post.PostDataDao
import com.takari.redditpostnotifier.data.subreddit.SubRedditData
import com.takari.redditpostnotifier.data.subreddit.SubRedditDataDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Repository @Inject constructor(
    private val redditApi: RedditApi,
    private val postDataDao: PostDataDao,
    private val subRedditDataDao: SubRedditDataDao,
    private val sharedPrefs: SharedPreferences
) {

    //Goes through a list of subRedditNames and retrieves the newest post for each of them
    suspend fun getPostList(vararg subName: String): Flow<List<Post>> = flow {
        subName.forEach { name ->
            val postList = redditApi.getNewPostList(name).data.children
            emit(postList)
        }
    }

    suspend fun getSubRedditData(name: String): SubRedditData =
        redditApi.getSubRedditData(name).data

    suspend fun insertPostDataInDb(post: PostData) = withContext(Dispatchers.Default) {
        postDataDao.insertReplace(post)
    }

    fun listenToPostDataInDb(): Flow<List<PostData>> {
        return postDataDao.listenForPostData()
    }

    suspend fun deletePostDataInDb(postData: PostData) {
        return postDataDao.deleteItem(postData)
    }

    suspend fun deleteAllDbPostData(): Unit {
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

    fun getIntFromSharedPrefs(key: String, defaultValue: Int): Int =
        sharedPrefs.getInt(key, defaultValue)

    fun saveIntToSharedPrefs(key: String, value: Int) {
        sharedPrefs.edit { putInt(key, value) }
    }
}