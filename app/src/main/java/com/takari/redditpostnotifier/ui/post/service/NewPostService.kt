package com.takari.redditpostnotifier.ui.post.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.App.Companion.applicationComponent
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.data.post.PostData
import com.takari.redditpostnotifier.data.subreddit.SubRedditData
import com.takari.redditpostnotifier.misc.logD
import com.takari.redditpostnotifier.ui.common.MainActivity
import com.takari.redditpostnotifier.ui.history.PostHistoryActivity
import com.takari.redditpostnotifier.ui.settings.SettingsActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


//Coroutines can be started from a service as long as we handle clean up. Config changes obvs wont be an issue.
class NewPostService : Service() {

    companion object {
        fun isRunning() = running
        const val RESET = "reset"
    }

    private val repository = applicationComponent().repository
    private val id = 2201
    private var newPostCounter = 0
    var onServiceDestroy: (() -> Unit) = {}
    private val viewedPost: MutableList<PostData> = mutableListOf() //used for filtering new post from old
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val currentTime = MutableLiveData("0")


    override fun onCreate() {
        super.onCreate()
        running = true
    }

    inner class LocalBinder : Binder() {
        fun getService(): NewPostService = this@NewPostService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return LocalBinder()
    }

    /*Only invoked on initialization and on reset.*/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent!!.action == RESET) {
            destroyService()
            return START_NOT_STICKY
        }

        val subRedditDataList: ArrayList<SubRedditData> = //late init var
            intent.getParcelableArrayListExtra(SubRedditData.SUB_REDDIT_DATA_NAME)!!

        val subNames = mutableListOf<String>()
        subRedditDataList.forEach { subNames.add(it.name) }

        val apiRequestRateInMillis: Long = repository.getIntFromSharedPrefs(
            key = SettingsActivity.API_REQUEST_RATE_KEY,
            defaultValue = 1
        ).toLong().toMilli()

        saveInitialPostInList(subNames.toTypedArray())

        listenForNewPost(apiRequestRateInMillis, subNames.toTypedArray())

        startForeground(id, getMainNotification("0"))
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
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

    private fun getMainNotification(timeInSeconds: String): Notification =
        NotificationCompat.Builder(this, App.CHANNEL_ID).apply {
            setSmallIcon(R.drawable.notification_icon)
            setContentTitle("No New Post Found Yet")
            setContentText("Connecting In: $timeInSeconds secs")
            addAction(R.drawable.reset, "Reset", resetIntent)
            setContentIntent(activityIntent)
            setOnlyAlertOnce(true)
        }.build()


    private fun showFoundPostNotification(postData: PostData, newPostCounter: Int) =
        NotificationCompat.Builder(this, App.CHANNEL_ID).apply {
            setSmallIcon(R.drawable.notification_icon)
            setContentTitle("Found $newPostCounter new post!")
            setContentText(postData.title)
            setAutoCancel(true)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setContentIntent(postHistoryIntent)
            setOnlyAlertOnce(true)
        }.build()

    private fun updateNotificationTimer(timeInSeconds: String) {
        NotificationManagerCompat.from(this).notify(id, getMainNotification(timeInSeconds))
    }

    private fun updateNewPostNotification(postData: PostData, newPostCounter: Int) {
        NotificationManagerCompat.from(this)
            .notify(238, showFoundPostNotification(postData, newPostCounter))
    }

    private val resetIntent: PendingIntent by lazy {
        val receiverIntent = Intent(this, NewPostReceiver::class.java).apply {
            action = RESET
        }
        PendingIntent.getBroadcast(this, 2, receiverIntent, 0)
    }

    private val postHistoryIntent: PendingIntent by lazy {
        val intent = Intent(this, PostHistoryActivity::class.java)
        PendingIntent.getActivity(this, 0, intent, 0)
    }

    private val activityIntent: PendingIntent by lazy {
        val intent = Intent(this, MainActivity::class.java)
        PendingIntent.getActivity(this, 0, intent, 0)
    }
}

private var running = false