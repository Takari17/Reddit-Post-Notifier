package com.takari.redditpostnotifier.data.subreddit

import androidx.room.*
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface SubRedditDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplace(subRedditData: SubRedditData): Single<Unit>

    @Query("SELECT * FROM sub_reddit_data")
    fun listenToSubRedditData(): Observable<List<SubRedditData>>

    @Query("SELECT * FROM sub_reddit_data")
    fun getCurrentSubRedditData(): Single<List<SubRedditData>>

    @Delete
    fun deleteItem(subRedditData: SubRedditData): Single<Unit>
}
