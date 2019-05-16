package com.example.androiddevhelper.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface Dao {

    @Insert
    fun insertPostData(postData: PostData): Completable

    @Query("SELECT * FROM post_data_table  ORDER BY id asc") // asc = ascending
    fun getAllPostData(): Observable<List<PostData>>

    @Query("DELETE FROM post_data_table")
    fun deleteAll(): Completable

    @Query("DELETE FROM post_data_table WHERE title ==:titleString ")
    fun deleteItem(titleString: String): Completable
}