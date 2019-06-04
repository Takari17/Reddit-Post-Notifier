package com.example.androiddevhelper.data

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.androiddevhelper.data.local.Dao
import com.example.androiddevhelper.data.local.PostData
import com.example.androiddevhelper.data.remote.FireStoreDb
import com.example.androiddevhelper.data.remote.reddit.NewRedditPost
import com.example.androiddevhelper.data.service.MainService
import com.example.androiddevhelper.data.service.isServiceRunning
import dagger.Reusable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


@Reusable
class Repository @Inject constructor(
    private val context: Context,
    private val fireStoreDb: FireStoreDb,
    private val localDb: Dao
) {

    fun saveListToFireStore(previousRedditPost: List<NewRedditPost>) =
        fireStoreDb.saveListToDb(previousRedditPost)

    //Fire Store query
    fun getPreviousRedditPost() = fireStoreDb.getListFromDb()

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
        if (!isServiceRunning) context.startService(MainService.createIntent(context))
    }

    fun resetService() {
        if (isServiceRunning) context.stopService(MainService.createIntent(context))
    }
}