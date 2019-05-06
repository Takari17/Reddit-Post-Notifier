package com.example.androiddevhelper.data

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.androiddevhelper.data.remote.FireStoreDb
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/*
todo need to save the value of previousRedditPost in firestore or Room

todo We're making it so that our db wont store any nulls (cause thats dumb) so we'll need to null check before we send the previous reddit post to the db

todo when are we gonna save out stuff to the db? Im thinking onpause and we do it in a service to play it save. Look up thoughts on this approach.

todo also when are we gonna GET the data from the db? On start obviously.

todo right now I want to write a few comments for future me, and I also want to try to rename some method and var names for better clarity. And just organize our code before its too late...
 */
class Repository(private val context: Application) {

    private val fireStoreDb = FireStoreDb()

    private var previousRedditPost: List<NewRedditPost> =
        emptyList() // used for filtering our duplicates, we compare new network call data to this

    private val compositeDisposable = CompositeDisposable()

    private val serviceIntent = Intent(context, MyService::class.java)

    fun saveListToFireStore() {
        if (previousRedditPost.isNotEmpty()) fireStoreDb.saveListToDb(previousRedditPost)
    }

    init {
        observeDbPreviousRedditPost() // Used for retreiving the list from firestore
    }


    private fun observeDbPreviousRedditPost() {
        compositeDisposable += fireStoreDb.getPreviousPostListFromDb
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = { list -> previousRedditPost = list },
                onError = { Log.d("zwi", "Error in \"setPreviousRedditPost\" method") }
            )
    }

    /*
    The Relay we observe will emit when the db transaction completes, there the value will be set
     */
    fun setPreviousRedditPost() = fireStoreDb.getListFromDb()

//    fun getAllNewRedditPost() = redditApi.getAllPostData()

    /*
    Filters new post for duplicates then sends the distinct new post to fireStore
     */
    fun saveNewPostToDb(redditPostList: List<NewRedditPost>) {

        compositeDisposable += filterOutDuplicates(redditPostList)
            .take(3)
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = { individualRedditPost -> fireStoreDb.saveNewPostToDb(individualRedditPost) },
                onComplete = {
                    previousRedditPost = redditPostList
                }, // only updates value after we've uploaded the data to the db, so we can compare the new network call data
                onError = { Log.d("zwi", "Error filtering out duplicates: $it") }
            )
    }

    private fun filterOutDuplicates(newPostList: List<NewRedditPost>): Observable<NewRedditPost> {
        /*
        Will only emit distinct values from previous network call, prevents the creation of duplicate documents in Firestore
         */
        return Observable.fromIterable(newPostList)
            .filter { newPost ->
                if (previousRedditPost.isEmpty()) true // all items pass the filter
                else newPost !in previousRedditPost //  compares new network call data to the previous network call data
            }
    }

    fun clearCompositeDisposable() = compositeDisposable.clear()


    fun startService() {
        context.apply {
            context.startService(serviceIntent)
            bindService(serviceIntent, serviceConnection, 0)
        }
    }


    fun resetService() {
        context.apply {
            stopService(serviceIntent)
            unbindService(serviceConnection)
        }
    }


    lateinit var myService: MyService

    val newRedditPostList = MutableLiveData<List<NewRedditPost>>()


    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val serviceReference = (service as MyService.LocalBinder).getService()

            myService = serviceReference

            myService.networkCallData.subscribeBy(
                onNext = {list ->
                    newRedditPostList.postValue(list)
                    saveNewPostToDb(list)
                },
                onError = {}
            )
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

    }


}