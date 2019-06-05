package com.example.androiddevhelper.data.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.androiddevhelper.App
import com.example.androiddevhelper.R
import com.example.androiddevhelper.data.local.PostData
import com.example.androiddevhelper.data.remote.PreviousRedditPost
import com.example.androiddevhelper.data.remote.reddit.NewRedditPost
import com.example.androiddevhelper.ui.activity.MainActivity
import com.example.androiddevhelper.utils.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/*
 *When created the service will make a network call every minute, and if they'res any
 *new reddit post it will prompt the user with a notification. When the notification is
 *clicked it will bring the user to reddit and open the clicked post.
 */
class MainService : Service() {

    companion object {
        var isRunning = false

        fun createIntent(context: Context) =
            Intent(context, MainService::class.java)
    }

    private val injector = App.applicationComponent
    private val repository = injector.repository

    private val redditApi = injector.redditApi
    private val sharedPrefs = injector.sharedPrefs
    private val compositeDisposable = CompositeDisposable()

    //Used for filtering our duplicates, we compare the new network call data to this
    private var previousNetworkCallData = emptyList<NewRedditPost>()

    //Temporally holds the new network call data so we can update "previousNetworkCallData" onComplete
    private var newNetworkCallData = emptyList<NewRedditPost>()

    //Increments with every new notification
    private var notificationId = 10126


    override fun onCreate() {
        super.onCreate()

        startNetworkCallInterval()

        restoreListData()

        isRunning = true
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createMainNotification())
        return START_STICKY
    }


    //Makes a network call every minute to Reddit's API
    private fun startNetworkCallInterval() {
        compositeDisposable += Observable.interval(1, TimeUnit.MINUTES)
            .map { getAllNewRedditPost() }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    /*
  Look at this monster.....what have I done xD
  This method takes the data fetched from r/AndroidDev and filters out the duplicate and applies a delay of 2 seconds between emissions
  so it's less jarring for the user if there's multiple new post.
   */
    private fun getAllNewRedditPost() {
        //Not a Single anymore so we have to dispose ;<
        compositeDisposable += redditApi.getAllPostData()
            .map { it.data.children }
            .flatMap { newPostList ->
                Observable.fromIterable(newPostList)
                    .filter { newPost -> newPost !in previousNetworkCallData }
                    .zipWith(Observable.interval(2, TimeUnit.SECONDS))
                    .map { pair ->
                        newNetworkCallData = newPostList
                        pair.first.data
                    }
            }
            .subscribeBy(
                onNext = { newPost ->
                    createNewNotification(newPost)
                    repository.insertPostDataToLocalDb(newPost)
                },
                onComplete = {
                    previousNetworkCallData = newNetworkCallData
                    if (previousNetworkCallData.isNotEmpty()) saveListFireStore(previousNetworkCallData)
                },
                onError = { e -> Log.d("zwi", "Error in getAllNewRedditPost() : $e") }
            )
    }

    private fun createNewNotification(newPost: PostData) =
        NotificationManagerCompat.from(this).apply {

            notify(
                notificationId, sharedPrefs.getNewPostNotification(
                    newPost.title,
                    newPost.description,
                    createNewPostPendingIntent(newPost.api)
                )
            )
            notificationId++
        }


    //Notification given to the foreground service, needs special attributes to host the group of new notifications
    private fun createMainNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.white_icon)
            setContentTitle("Android Dev Helper!")
            setContentText("Waiting For New Reddit Post...")
            setGroupSummary(true)
            setGroup(CUSTOM_GROUP_ID)
            setCategory(NotificationCompat.CATEGORY_MESSAGE)
            setColorized(true)
            setContentIntent(createActivityPendingIntent())
        }.build()


    //Creates a new pending intent with it's action set to the reddit post url (for onClick functionality)
    private fun createNewPostPendingIntent(url: String): PendingIntent =
        PendingIntent.getBroadcast(
            this, 0, MainBroadcastReceiver.createIntent(this, url), 0
        )

    private fun createActivityPendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            this, 0, MainActivity.createIntent(this), 0
        )


    private fun saveListFireStore(list: List<NewRedditPost>) =
        repository.saveListToFireStore(list)


    //Pulls the saved list from firestore and sets it to the previousNetworkCallData list, method takes 2-3 seconds to finish
    private fun restoreListData() {
        repository.getPreviousRedditPost()
            .addOnSuccessListener { documentData ->

                if (documentData.exists()) {
                    val previousPostObject = documentData.toObject(PreviousRedditPost::class.java)!!
                    val savedList = previousPostObject.previousRedditPost
                    previousNetworkCallData = savedList
                }

            }.addOnFailureListener { Log.d("zwi", "Failed getting post from firestore") }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        isRunning = false
    }

    // Receives click events from any new post notification and opens the URL for that post
    class MainBroadcastReceiver : BroadcastReceiver() {

        companion object {
            fun createIntent(context: Context, action: String? = null) =
                Intent(context, MainBroadcastReceiver::class.java).apply {
                    this.action = action
                }
        }

        override fun onReceive(context: Context, intent: Intent) {
            val api = intent.action
            if (api != BASE_URL) openRedditPost(context, api!!)
        }
    }
}