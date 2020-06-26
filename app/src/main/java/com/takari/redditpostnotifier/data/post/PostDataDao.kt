package com.takari.redditpostnotifier.data.post

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplace(postData: PostData)

    @Query("SELECT * FROM post_data_table")
    fun listenForPostData(): Flow<List<PostData>>

    @Delete
    suspend fun deleteItem(vararg postData: PostData)

    @Query("DELETE FROM post_data_table")
    suspend fun deleteAll()
}