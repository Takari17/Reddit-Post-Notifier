package com.takari.redditpostnotifier.features.reddit.newPost.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.features.MainActivity
import com.takari.redditpostnotifier.features.reddit.data.Repository
import com.takari.redditpostnotifier.features.reddit.newPost.models.PostData
import com.takari.redditpostnotifier.features.reddit.subreddit.models.SubRedditData
import com.takari.redditpostnotifier.features.settings.SettingsFragment
import com.takari.redditpostnotifier.utils.logD
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class NewPostService : Service() {

    companion object {
        fun isRunning() = running
        const val RESET = "reset"
    }

    @Inject
    lateinit var repository: Repository

    private val id = 2201
    private var newPostCounter = 0
    var onServiceDestroy: (() -> Unit) = {}
    private val viewedPost: MutableList<PostData> =
        mutableListOf() //used for filtering new post from old
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val currentTime = MutableLiveData("0")
    private val notificationBuilder by lazy { NotificationCompat.Builder(this, App.CHANNEL_ID) }
    private val notificationManager by lazy { NotificationManagerCompat.from(this) }

    override fun onCreate() {
        super.onCreate()
        running = true
    }

    inner class LocalBinder : Binder() {
        fun getService(): NewPostService = this@NewPostService
    }

    override fun onBind(intent: Intent?): IBinder {
        return LocalBinder()
    }

    /*Only invoked on initialization and on reset.*/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        logD("SERVICE STARTED ----------------------------------------")

        if (intent!!.action == RESET) {
            destroyService()
            return START_NOT_STICKY
        }

        val subRedditDataList: ArrayList<SubRedditData> = //late init var
            intent.getParcelableArrayListExtra(SubRedditData.SUB_REDDIT_DATA_NAME)!!

        val subNames = mutableListOf<String>()
        subRedditDataList.forEach { subNames.add(it.name) }

        val apiRequestRateInMillis: Long = repository.getIntFromSharedPrefs(
            key = SettingsFragment.API_REQUEST_RATE_KEY,
            defaultValue = 1
        ).toLong().toMilli()

        saveInitialPostInList(subNames.toTypedArray())

        listenForNewPost(apiRequestRateInMillis, subNames.toTypedArray())

        val session = MediaSessionCompat(this, "tag").sessionToken
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(session)

        val notification = notificationBuilder.apply {
            setSmallIcon(R.drawable.notification_icon)
            setStyle(mediaStyle)
            setContentTitle("No New Post Found Yet")
            setContentText("Connecting In...")
            addAction(R.drawable.reset, "Reset", resetIntent)
            setContentIntent(activityIntent)
            setOnlyAlertOnce(true)
        }.build()

        startForeground(id, notification)

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        logD("SERVICE DESTROYED --------------------------------------------")
        running = false
        scope.cancel()
        onServiceDestroy()
    }

    private fun destroyService() {
        stopSelf()
        stopForeground(true)
    }

    //Used for future filtering comparisons. Filters through a long list so Dispatchers.Default is used
    private fun saveInitialPostInList(subNames: Array<String>) {
        scope.launch {
            repository.getPostList(*subNames)
                .flowOn(Dispatchers.Default)
                .collect { postList -> postList.forEach { post -> viewedPost.add(post.data) } }
        }
    }

    //Don't have to call stop bc it'll be destroyed when the scope clears.
    private fun flowRepeatingCountDownTimer(startingMillis: Long) = flow {
        var currentTime = startingMillis
        while (true) {
            currentTime -= 1000
            emit(currentTime.toSecondsFormat())
            delay(1000)
            if (currentTime <= 0L) {
                currentTime = startingMillis
            }
        }
    }

    private fun listenForNewPost(apiRequestRateInMillis: Long, subNames: Array<String>) {
        scope.launch {
            flowRepeatingCountDownTimer(apiRequestRateInMillis)
                .map {
                    currentTime.value = it
                    updateNotificationTimer(it)
                    it
                }
                .filter { it == "0" }
                .flatMapMerge { repository.getPostList(*subNames) }
                .flatMapMerge { it.asFlow() }
                .filter { it.data !in viewedPost }
                .catch { logD(it.message) }
                .retry { true } // retries on any exception
                .collect { post ->
                    viewedPost.add(post.data)
                    newPostCounter++
                    updateNewPostNotification(post.data, newPostCounter)
                    repository.insertPostDataInDb(post.data)
                }
        }
    }

    private fun Long.toSecondsFormat(): String {
        val seconds = (this / 1000).toInt()
        return seconds.toString()
    }

    private fun Long.toMilli(): Long = (this * 60000)

    fun observeCurrentTime(): LiveData<String> = currentTime

    private fun getFoundPostNotification(postData: PostData, newPostCounter: Int): Notification {
        val session = MediaSessionCompat(this, "tag").sessionToken
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(session)

        return NotificationCompat.Builder(this, App.CHANNEL_ID).apply {
            setSmallIcon(R.drawable.notification_icon)
            setStyle(mediaStyle)
            setContentIntent(activityIntent)
            setContentTitle("Found $newPostCounter new post!")
            setContentText(postData.title)
            setAutoCancel(true)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setOnlyAlertOnce(true)
        }.build()
    }

    private fun updateNotificationTimer(timeInSeconds: String) {
        notificationBuilder.setContentText("Connecting in: $timeInSeconds seconds")

        notificationManager.notify(id, notificationBuilder.build())
    }

    private fun updateNewPostNotification(postData: PostData, newPostCounter: Int) {
        val newNotification = getFoundPostNotification(postData, newPostCounter)
        notificationManager.notify(238, newNotification)
    }

    private val resetIntent: PendingIntent by lazy {
        val receiverIntent = Intent(this, NewPostReceiver::class.java).apply {
            action = RESET
        }
        PendingIntent.getBroadcast(this, 2, receiverIntent, PendingIntent.FLAG_IMMUTABLE)
    }

//    private val postHistoryIntent: PendingIntent by lazy {
//        val intent = Intent(this, PostHistoryActivity::class.java)
//        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//    }

    private val activityIntent: PendingIntent by lazy {
        val intent = Intent(this, MainActivity::class.java)
        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}

private var running = false