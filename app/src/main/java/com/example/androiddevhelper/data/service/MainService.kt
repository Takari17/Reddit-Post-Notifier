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

var isServiceRunning = false

/**
 *When created the service will make a network call every minute, and if they'res any
 *new reddit post it will prompt the user with a notification. When the notification is
 *clicked it will bring the user to reddit and open the clicked post.
 */
class MainService : Service() {

    companion object {
        fun createIntent(context: Context) =
            Intent(context, MainService::class.java)

    }

    private val injector = App.applicationComponent

    private val repository = injector.repository
    private val redditApi = injector.redditApi
    private val sharedPrefs = injector.sharedPrefs

    private val compositeDisposable = CompositeDisposable()

    //Used for filtering our duplicates, we compare the new network call data to this
    private var previousRedditPost: List<NewRedditPost> = emptyList()

    //todo use by lazy for pending intents for immutability
    private lateinit var mainContentIntent: PendingIntent
    private lateinit var postContentIntent: PendingIntent

    //Notification id which increments with every new notification for distinct id's
    private var id = 10126

    private fun createNewNotification(newPost: PostData) {

        NotificationManagerCompat.from(this).apply {

            postContentIntent = createNewPostPendingIntent(newPost.api)

            notify(id, sharedPrefs.getNewPostNotification(newPost.title, newPost.description, postContentIntent))

            id++
        }
    }


    private fun executeNetworkCall() = redditApi.getAllPostData()

    private fun getAllNewRedditPost() {
        //No need to dispose a Single :D
        executeNetworkCall()
            .subscribeOn(Schedulers.io())
            .map { it.data.children }
            .subscribeBy(
                onSuccess = { list -> filterOutDuplicates(list) },
                onError = { Log.d("zwi", "Failed executing network call in getAllNewRedditPost(), error: $it") }
            )
    }

    /*
    Look at this monster.....what have I done xD
    This method takes the data fetched from r/AndroidDev and filters out the duplicate post by comparing
    the new data to the previous data fetched. It then applies a delay of 2 seconds between emissions
    so it's less jarring for the user if there's multiple new post
     */
    private fun filterOutDuplicates(newPostList: List<NewRedditPost>) {
        Observable.fromIterable(newPostList)
            .subscribeOn(Schedulers.io())
            .filter { individualPost ->
                if (previousRedditPost.isEmpty()) true // all items pass the filter
                else individualPost !in previousRedditPost //  compares new network call data to the one previously made, then emits any new values
            }
            .zipWith(Observable.interval(2, TimeUnit.SECONDS)) //Delay of 2 seconds for every new notification
            .map { pair -> pair.first.data }
            .subscribeBy(
                onNext = { individualPost ->
                    createNewNotification(individualPost)
                    repository.insertPostDataToLocalDb(individualPost)
                },

                onComplete = {
                    //This is so it can compare the next network call
                    previousRedditPost = newPostList.also {
                        if (previousRedditPost.isNotEmpty()) saveListFireStore(previousRedditPost)
                    }
                },
                onError = { e -> Log.d("zwi", "Error filtering out duplicates: $e") }
            )
    }

    override fun onCreate() {
        super.onCreate()
        startNetworkCallInterval()

        mainContentIntent = createActivityPendingIntent()
        postContentIntent = createNewPostPendingIntent(NOTIF_CLICK)

        //Comment this out for testing
        restoreListData()

        isServiceRunning = true
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createMainNotification())
        return START_STICKY
    }

    //Notification given to the foreground service, needs special attributes to host the group of new notifications
    private fun createMainNotification(): Notification {

        return NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.white_icon)
            setContentTitle("Android Dev Helper!")
            setContentText("Waiting For New Reddit Post...")
            setGroupSummary(true)
            setGroup(CUSTOM_GROUP_ID)
            setCategory(NotificationCompat.CATEGORY_MESSAGE)
            setColorized(true)
            setContentIntent(mainContentIntent)
        }.build()
    }


    /*
    Makes a network call every minute, filters out the duplicate post, then prompts the
    user with a notification for every new distinct post
    */
    private fun startNetworkCallInterval() {
        compositeDisposable += Observable.interval(1, TimeUnit.MINUTES)
            .map { getAllNewRedditPost() }
            .subscribe()
    }

    //Creates a new pending intent with it's action set to the reddit post url (for onClick functionality)
    private fun createNewPostPendingIntent(actionUrl: String): PendingIntent =
        Intent(this, MyBroadcastReceiver::class.java).apply {
            action = actionUrl
        }.let { broadcastIntent ->
            PendingIntent.getBroadcast(
                this, 0, broadcastIntent, 0
            )
        }

    //When the main foreground notification is clicked it will bring the user to the main activity
    private fun createActivityPendingIntent(): PendingIntent {
        return Intent(this, MainActivity::class.java).let { activityIntent ->
            PendingIntent.getActivity(
                this, 0, activityIntent, 0
            )
        }
    }

    //Saves the previousRedditPost list to firestore, restored on service create
    private fun saveListFireStore(previousRedditPost: List<NewRedditPost>) {
        repository.saveListToFireStore(previousRedditPost)
    }

    //Pulls the saved list from firestore and sets it to the previousRedditPost list, method takes 2-3 seconds to finish
    private fun restoreListData() {
        repository.getPreviousRedditPost()
            .addOnSuccessListener { documentData ->

                if (documentData.exists()) {
                    val previousPostObject = documentData.toObject(PreviousRedditPost::class.java)!!
                    val savedList = previousPostObject.previousRedditPost
                    previousRedditPost = savedList
                }

            }.addOnFailureListener { Log.d("zwi", "Failed getting post from firestore") }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        isServiceRunning = false
    }

    // Receives click events from any new post notification and opens the URL for that post
    class MyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val api = intent.action
            if (api != BASE_URL) openRedditPost(context, api!!)
        }
    }
}