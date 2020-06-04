package com.takari.redditpostnotifier.data.misc

import androidx.room.Database
import androidx.room.RoomDatabase
import com.takari.redditpostnotifier.data.post.PostData
import com.takari.redditpostnotifier.data.post.PostDataDao
import com.takari.redditpostnotifier.data.subreddit.SubRedditData
import com.takari.redditpostnotifier.data.subreddit.SubRedditDataDao
import javax.inject.Singleton

@Singleton
@Database(entities = [PostData::class, SubRedditData::class], version = 1)
abstract class RoomDb : RoomDatabase() {

    companion object {
        const val DB_NAME = "post_db"
    }

    abstract val postDataDao: PostDataDao
    abstract val subRedditDataDao: SubRedditDataDao
}