package com.takari.redditpostnotifier.features.reddit.subreddit.models

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubRedditDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplace(subRedditData: SubRedditData)

    @Query("SELECT * FROM sub_reddit_data")
    fun listenToSubRedditData(): Flow<List<SubRedditData>>

    @Query("SELECT * FROM sub_reddit_data")
    suspend fun getCurrentSubRedditData(): List<SubRedditData>

    @Delete
    suspend fun deleteItem(subRedditData: SubRedditData)
}
