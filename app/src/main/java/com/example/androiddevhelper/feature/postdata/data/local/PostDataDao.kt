package com.example.androiddevhelper.feature.postdata.data.local

import androidx.room.*
import com.example.androiddevhelper.feature.postdata.data.local.PostData
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Singleton

@Singleton
@Dao
interface PostDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg post: PostData): Completable

    @Query("SELECT * FROM reddit_post_data_table  ORDER BY id asc")
    fun listen(): Observable<List<PostData>>

    @Query("DELETE FROM reddit_post_data_table WHERE title ==:postTitle ")
    fun deleteItem(vararg postTitle: String): Completable

    @Query("DELETE FROM reddit_post_data_table")
    fun deleteAll(): Completable
}
