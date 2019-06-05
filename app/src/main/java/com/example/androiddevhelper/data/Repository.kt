package com.example.androiddevhelper.data

import android.app.PendingIntent
import android.content.Context
import com.example.androiddevhelper.data.local.Dao
import com.example.androiddevhelper.data.local.PostData
import com.example.androiddevhelper.data.local.SharedPrefs
import com.example.androiddevhelper.data.remote.FireStoreDb
import com.example.androiddevhelper.data.remote.reddit.NewRedditPost
import com.example.androiddevhelper.data.remote.reddit.RedditApi
import com.example.androiddevhelper.data.service.MainService
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Repository @Inject constructor(
    private val context: Context,
    private val fireStoreDb: FireStoreDb,
    private val redditApi: RedditApi,
    private val localDb: Dao,
    private val sharedPrefs: SharedPrefs
) {

    fun saveListToFireStore(previousRedditPost: List<NewRedditPost>) =
        fireStoreDb.saveListToDb(previousRedditPost)

    //Fire Store query
    fun getPreviousRedditPost() = fireStoreDb.getListFromDb()

    fun executeGetAllPostData() = redditApi.getAllPostData()

    fun getNewPostNotification(
        title: String,
        description: String,
        postContentIntent: PendingIntent
    ) = sharedPrefs.getNewPostNotification(title, description, postContentIntent)

    //Local Db Methods
    fun insertPostDataToLocalDb(postData: PostData): Disposable =
        localDb.insertPostData(postData)
            .subscribeOn(Schedulers.io())
            .subscribe()

    //Observes any changes made to our local data base
    fun observeAllPostData(): Observable<List<PostData>> =
        localDb.getAllPostData()
            .subscribeOn(Schedulers.io())

    //Deletes the specific item from the local db that matches the value passed
    fun deleteItem(titleString: String): Disposable =
        localDb.deleteItem(titleString)
            .subscribeOn(Schedulers.io())
            .subscribe()


    fun startService() {
        if (!MainService.isRunning) context.startService(MainService.createIntent(context))
    }

    fun resetService() {
        if (MainService.isRunning) context.stopService(MainService.createIntent(context))
    }
}