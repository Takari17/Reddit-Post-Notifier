package com.example.androiddevhelper.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost
import io.reactivex.Completable
import io.reactivex.Single

/*
Methods that will interact with the database
 */
@Dao
interface Dao {

    @Insert
    fun insertPreviousRedditPost(previousRedditPost: DbData): Completable

//    @Query("SELECT previousRedditPost FROM data_table")
//    fun getPreviousRedditPost(): Single<List<NewRedditPost>>

    @Query("DELETE FROM data_table")
    fun deleteAll(): Completable
}
