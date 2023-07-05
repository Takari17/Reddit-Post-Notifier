package com.takari.redditpostnotifier.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.takari.redditpostnotifier.features.reddit.data.RedditApi
import com.takari.redditpostnotifier.features.reddit.data.RedditApi.Companion.BASE_URL
import com.takari.redditpostnotifier.features.reddit.data.RoomDb
import com.takari.redditpostnotifier.features.reddit.data.RoomDb.Companion.DB_NAME
import com.takari.redditpostnotifier.features.reddit.newPost.models.PostDataDao
import com.takari.redditpostnotifier.features.reddit.subreddit.models.SubRedditDataDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    fun provideRoomDb(@ApplicationContext context: Context): RoomDb {
        return Room.databaseBuilder(context, RoomDb::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePostDataDao(roomDb: RoomDb): PostDataDao {
        return roomDb.postDataDao
    }

    @Provides
    fun provideSubRedditDataDao(roomDb: RoomDb): SubRedditDataDao {
        return roomDb.subRedditDataDao
    }

    @Provides
    fun provideRetrofit(): RedditApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RedditApi::class.java)
    }
}