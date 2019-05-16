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

    private val serviceIntent = Intent(context, MainService::class.java)
    private val compositeDisposable = CompositeDisposable()

    fun saveListToFireStore(previousRedditPost: List<NewRedditPost>) {
        if (previousRedditPost.isNotEmpty()) fireStoreDb.saveListToDb(previousRedditPost)
    }

    //Fire Store query
    fun getPreviousRedditPost() = fireStoreDb.getListFromDb()


    //Local Db Methods
    fun insertPostDataToLocalDb(postData: PostData): Disposable =
        localDb.insertPostData(postData)
            .subscribeOn(Schedulers.io())
            .doOnComplete { Log.d("zwi", "Post inserted into local db") }
            .subscribe()

    //Observes any changes made to our local data base
    fun observeAllPostData(): Observable<List<PostData>> =
        localDb.getAllPostData()
            .subscribeOn(Schedulers.io())

    fun deleteLocalDbData(): Disposable =
        localDb.deleteAll()
            .subscribeOn(Schedulers.io())
            .subscribe()

    //Deletes the specific item from our local db that matches the value passed
    fun deleteItem(titleString: String): Disposable =
        localDb.deleteItem(titleString)
            .subscribeOn(Schedulers.io())
            .doOnComplete { Log.d("zwi", "Item deleted from db") }
            .subscribe()


    //Cleared from ViewModel's onClear callback
    fun clearCompositeDisposable() = compositeDisposable.clear()

    fun startService() {
        if (!isServiceRunning) context.startService(serviceIntent)
    }

    fun resetService() {
        if (isServiceRunning) context.stopService(serviceIntent)
    }
}