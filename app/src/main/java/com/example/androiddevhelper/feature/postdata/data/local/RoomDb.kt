package com.example.androiddevhelper.feature.postdata.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import javax.inject.Singleton

@Singleton
@Database(entities = [PostData::class], version = 1)
abstract class RoomDb : RoomDatabase() {
    abstract val postDataDao: PostDataDao

    companion object{
        const val POST_DATA_DB = "Post Data Database"
    }
}