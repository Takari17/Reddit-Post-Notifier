package com.example.androiddevhelper.injection

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room

import com.example.androiddevhelper.feature.postdata.data.local.PostDataDao
import com.example.androiddevhelper.feature.postdata.data.local.RoomDb
import com.example.androiddevhelper.feature.postdata.data.local.RoomDb.Companion.POST_DATA_DB
import com.example.androiddevhelper.feature.postdata.data.remote.RedditApi
import com.example.androiddevhelper.feature.postdata.data.remote.RedditApi.Companion.BASE_URL
import com.example.androiddevhelper.feature.postdata.ui.PostDataFragment
import com.example.androiddevhelper.feature.postdata.ui.PostDataUIState
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
object ApplicationModule {

    @JvmStatic
    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)


    @JvmStatic
    @Provides
    fun providePostDataDao(context: Context): PostDataDao =
        Room.databaseBuilder(context.applicationContext, RoomDb::class.java, POST_DATA_DB)
            .fallbackToDestructiveMigration()
            .build()
            .postDataDao


    @JvmStatic
    @Provides
    fun provideRetrofit(): RedditApi {
        //Tells retrofit to ignore the POJO values that DON"T have the Expose annotation (used so Retrofit ignores Room's primary key)
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(RedditApi::class.java)
    }


    @JvmStatic
    @Provides
    fun notificationId(): Int = 2201


    @JvmStatic
    @Provides
    fun provideDefaultState(): PostDataUIState =
        PostDataUIState(
            subRedditStatus = PostDataFragment.SubRedditStatus.Empty,
            isLoading = false,
            postDataList = emptyList(),
            subReddit = ""
        )
}