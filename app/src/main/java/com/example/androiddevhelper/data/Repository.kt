package com.example.androiddevhelper.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.androiddevhelper.data.remote.FireStoreDb
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/*

todo right now I want to write a few comments for future me, and I also want to try to rename some method and var names for better clarity. And just organize our code before its too late...

Maybe clear our disposables onStop instead of onDestroy, I mean do we really need them onStop anyways?
 */

class Repository(private val context: Context) {

    companion object {
        @Volatile
        private var instance: Repository? = null

        operator fun invoke(context: Context): Repository {
            if (instance == null) instance = Repository(context)
            return instance!!
        }
    }

    private val fireStoreDb = FireStoreDb()

    private val compositeDisposable = CompositeDisposable()

    private val serviceIntent = Intent(context, MyService::class.java)

    private lateinit var myService: MyService

    val newRedditPostList = MutableLiveData<List<NewRedditPost>>()

    fun saveListToFireStore(previousRedditPost: List<NewRedditPost>) {
        if (previousRedditPost.isNotEmpty()) fireStoreDb.saveListToDb(previousRedditPost)
    }

    //Fire Store query
    fun getPreviousRedditPost() = fireStoreDb.getListFromDb()

    //Cleared from ViewModel's onClear method
    fun clearCompositeDisposable() = compositeDisposable.clear()

    fun startService() {
        context.apply {
            startService(serviceIntent)
            bindService(serviceIntent, serviceConnection, 0)
        }
    }


    fun resetService() {
        context.apply {
            stopService(serviceIntent)
            unbindService(serviceConnection)
        }
    }

    //Binds onStart if the service is running
    fun bindToService() {
        if (isServiceRunning){
            context.bindService(serviceIntent, serviceConnection, 0)
        }
    }

    //Unbinds onStop if the service is running
    fun unbindFromService() {
        if(isServiceRunning){
            context.unbindService(serviceConnection)
        }
    }

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            myService = (service as MyService.LocalBinder).getService()

            compositeDisposable += myService.newRedditPostList
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onNext = { newPostData -> newRedditPostList.postValue(newPostData) },
                    onError = { Log.d ("zwi", "Error fetching newRedditPostList from inside serviceConnection (Repository)")}
                )
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }
}