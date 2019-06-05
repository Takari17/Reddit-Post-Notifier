package com.example.androiddevhelper.injection

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.example.androiddevhelper.data.local.Dao
import com.example.androiddevhelper.data.local.DataBase
import com.example.androiddevhelper.data.remote.reddit.RedditApi
import com.example.androiddevhelper.utils.BASE_URL
import com.example.androiddevhelper.utils.DB_TABLE_NAME
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideLocalDb(context: Context): Dao =
        Room.databaseBuilder(context.applicationContext, DataBase::class.java, DB_TABLE_NAME)
            .build()
            .dao()


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
    fun provideFireStore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @JvmStatic
    @Provides
    fun provideRecentPostDocument(fireStore: FirebaseFirestore): DocumentReference =
        fireStore.collection("New Reddit Post ").document("Previous Reddit Post")
}