package com.takari.redditpostnotifier.data.post

import androidx.room.*
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface PostDataDao {

    /*
    I chose to just use Single<Unit> instead of Completables to limit the amount of
    Observable types used. Converting Observable types was a pain...
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplace(vararg postData: PostData): Single<Unit>

    @Query("SELECT * FROM post_data_table")
    fun listenForPostData(): Observable<List<PostData>>

    @Delete
    fun deleteItem(vararg postData: PostData): Single<Unit>

    @Query("DELETE FROM post_data_table")
    fun deleteAll(): Single<Unit>
}