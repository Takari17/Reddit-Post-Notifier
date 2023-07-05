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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
object ApplicationModule {

    @JvmStatic
    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @JvmStatic
    @Provides
    fun provideRoomDb(context: Context): RoomDb =
        Room.databaseBuilder(context, RoomDb::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @JvmStatic
    @Provides
    fun providePostDataDao(roomDb: RoomDb): PostDataDao =
        roomDb.postDataDao

    @JvmStatic
    @Provides
    fun provideSubRedditDataDao(roomDb: RoomDb): SubRedditDataDao =
        roomDb.subRedditDataDao


    @JvmStatic
    @Provides
    fun provideRetrofit(): RedditApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RedditApi::class.java)
    }
}