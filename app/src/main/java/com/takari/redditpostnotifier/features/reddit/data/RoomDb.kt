package com.takari.redditpostnotifier.features.reddit.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.takari.redditpostnotifier.features.reddit.newPost.models.PostData
import com.takari.redditpostnotifier.features.reddit.newPost.models.PostDataDao
import com.takari.redditpostnotifier.features.reddit.subreddit.models.SubRedditData
import com.takari.redditpostnotifier.features.reddit.subreddit.models.SubRedditDataDao
import javax.inject.Singleton

@Singleton
@Database(entities = [PostData::class, SubRedditData::class], version = 2)
abstract class RoomDb : RoomDatabase() {

    companion object {
        const val DB_NAME = "post_db"
    }

    abstract val postDataDao: PostDataDao
    abstract val subRedditDataDao: SubRedditDataDao
}