package com.example.androiddevhelper.data

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.androiddevhelper.R
import com.example.androiddevhelper.data.remote.PreviousRedditPost
import com.example.androiddevhelper.data.remote.reddit.RedditApi
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost
import com.example.androiddevhelper.ui.MainActivity
import com.example.androiddevhelper.utils.*
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


var isServiceRunning = false

/**
 *When created the service will make a network call every minute, and if they'res any
 *new reddit post it will prompt the user with a notification. When the notification is
 *clicked it will bring the user to reddit, then open the clicked post.
 */


/*
Our recycler view crashes if we emit more than 6 values at once, so to fix this porblem we'll use
Rx javas back pressure to slow down the rate a bit

But because of this we'll have to use a Flowable instead of our Relay, which is fine, don't see any cons of
switching to a Flowable and calling onNext, just be sure you dispose
 */
class MyService : Service() {

    private val repository = Repository.invoke(this)
    private val compositeDisposable = CompositeDisposable()
    /*
    Observed by repo, emits the filtered list data so the recycler view can be populated
    or it can be saved to firestore
     */
    val newRedditPostList =
        BehaviorRelay.createDefault(emptyList<NewRedditPost>())  // needs to be a behavior relay so when our view re subscribes our recycler view gets updated right away

    private val redditApi = RedditApi.invoke()

    //Used for filtering our duplicates, we compare new network call data to this
    private var previousRedditPost: List<NewRedditPost> = emptyList() // empty list by default instead of null

    //Holds the values of the previousRedditPost list after it's been filtered for duplicates
    private val filteredPostData = mutableListOf<NewRedditPost>()

    private lateinit var mainContentIntent: PendingIntent
    private lateinit var postContentIntent: PendingIntent


    //Notification Id, increments with every new Notification
    var id = 10126

    private fun createNewNotification(filteredList: List<NewRedditPost>) {

        NotificationManagerCompat.from(this).apply {

            filteredList.forEach {
                val redditPostData = it.data

                postContentIntent = createNewPostPendingIntent(redditPostData.api)

                notify(id, createNewPostNotification(redditPostData.title))

                id++
            }
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
                onError = {}
            )
    }

    private fun filterOutDuplicates(newPostList: List<NewRedditPost>) {
        /*
        Will only emit distinct values from previous network call
         */
        Observable.fromIterable(newPostList)
            .subscribeOn(Schedulers.io())
            .take(6)
            .filter { individualPost ->
                if (previousRedditPost.isEmpty()) true // all items pass the filter
                else individualPost !in previousRedditPost //  compares new network call data to the one previously made, then emits any new values
            }
            .subscribeBy(
                onNext = { individualPost -> filteredPostData.add(individualPost) },

                onComplete = {
                    newRedditPostList.accept(filteredPostData)
                    createNewNotification(newRedditPostList.value!!)
                    previousRedditPost = newPostList // saves so we can compare the next network call
                    saveListFireStore(previousRedditPost) // saves list to firestore after every network call
                    filteredPostData.clear()
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
//        restoreListData()

        isServiceRunning = true
    }

    override fun onBind(intent: Intent?): IBinder? = LocalBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(NOTIFICATION_ID, createMainNotification())
        return START_NOT_STICKY
    }


    //Notification given to the foreground service, needs special attributes to host the group of any new notifications
    private fun createMainNotification(): Notification {

        return NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.green_android_icon)
            setContentTitle("This is the Title")
            setGroupSummary(true)
            setGroup(CUSTOM_GROUP_ID)
            setStyle(NotificationCompat.BigTextStyle().bigText("Awaiting Network Call..."))
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(NotificationCompat.CATEGORY_MESSAGE)
            color = Color.GREEN
            setContentIntent(mainContentIntent)
        }.build()
    }

    //Notification given to any new post
    private fun createNewPostNotification(contentText: String): Notification {

        return NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.green_android_icon)
            setContentTitle("New Reddit Post Found!")
            setContentText(contentText)
            setContentIntent(postContentIntent)
            setGroup(CUSTOM_GROUP_ID)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_HIGH
            setDefaults(NotificationCompat.DEFAULT_SOUND)
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
    private fun createNewPostPendingIntent(actionUrl: String): PendingIntent {
        return Intent(this, MyBroadcastReceiver::class.java).apply {
            this.action = actionUrl
        }.let { broadcastIntent ->
            PendingIntent.getBroadcast(
                this, 0, broadcastIntent, 0
            )
        }
    }

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

    inner class LocalBinder : Binder() {
        fun getService(): MyService = this@MyService
    }

    // Receives click events from any new post notification and opens the URL for that post
    class MyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val api = intent.action.let { api ->
                BASE_URL + api
            }
            openRedditPost(context, api)
        }

        //todo gotta find some way to make this run on a background thread, maybe use coroutines?
        private fun openRedditPost(context: Context, url: String) {
            Intent(
                Intent.ACTION_VIEW, Uri.parse(url)
            ).also { intent -> context.startActivity(intent) }
        }
    }
}