package com.takari.redditpostnotifier.ui.post.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.App.Companion.applicationComponent
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.data.post.PostData
import com.takari.redditpostnotifier.data.subreddit.SubRedditData
import com.takari.redditpostnotifier.ui.common.MainActivity
import com.takari.redditpostnotifier.ui.history.PostHistoryActivity
import com.takari.redditpostnotifier.ui.settings.SettingsActivity
import com.takari.redditpostnotifier.misc.logD
import com.takari.redditpostnotifier.misc.RepeatingCountDownTimer
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/*
  The service should act live a view, it should only observe observables exposed to it. It should not contain observables.
  I think it's ok to have state in your service since it's not stifled by too many life cycle stuff. Just be sure to isolate
  heavy logic
 */
class NewPostService : Service() {

    companion object {
        fun isRunning() = running
        const val RESET = "reset"
    }

    private val repository = applicationComponent().repository
    private val compositeDisposable = CompositeDisposable()
    private var isSubscribed = false
    private var id = 2201
    private var newPostCounter = 0
    val reset = PublishRelay.create<Unit>()
    val repeatingCountDownTimer = BehaviorRelay.create<String>()
    //used for filtering new post from old
    private val viewedPost: MutableList<PostData> = mutableListOf()

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

    private val activityIntent:  PendingIntent by lazy {
        val intent = Intent(this, MainActivity::class.java)
        PendingIntent.getActivity(this, 0, intent, 0)
    }


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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent!!.action == RESET) {
            reset.accept(Unit)
            destroyService()
            return START_NOT_STICKY
        }

        if (!isSubscribed) {

            val subRedditDataList: ArrayList<SubRedditData> =
                intent.getParcelableArrayListExtra(SubRedditData.SUB_REDDIT_DATA_NAME)!!

            val apiRequestRateInMillis: Long = repository.getIntFromSharedPrefs(
                key = SettingsActivity.API_REQUEST_RATE_KEY,
                defaultValue = 1
            ).toLong().toMilli()

            val _repeatingCountDownTimer = RepeatingCountDownTimer(apiRequestRateInMillis)

            compositeDisposable += repository.getMultiplePostDataList(subRedditDataList)
                .doOnSubscribe { isSubscribed = true }
                .doOnDispose { isSubscribed = false }
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onNext = { postList -> postList.forEach { post -> viewedPost.add(post.data) } },
                    onError = { logD("Error making initial api call in NewPostService: $it") }
                )

            compositeDisposable += _repeatingCountDownTimer.get
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onNext = { timeInSeconds ->
                        repeatingCountDownTimer.accept(timeInSeconds)
                        updateNotificationTimer(timeInSeconds)
                    },
                    onError = { e -> logD("Error observing_RepeatingCountDownTimer in NewPostService: $e") }
                )

            compositeDisposable += repository.getNewPostDataWithInterval(
                apiRequestRateInMillis,
                subRedditDataList,
                viewedPost
            )
                .subscribeOn(Schedulers.io())
                .flatMapSingle { postData ->
                    //Updates the view's recycler view since it observes the db
                    repository.insertPostDataInDb(postData)
                        .map { postData }
                }
                .subscribeBy(
                    onNext = { postData ->
                        newPostCounter++
                        updateNewPostNotification(postData, newPostCounter)
                    },
                    onError = { logD("Error observing for new post in NewPostService: $it") }
                )
        }

        startForeground(id, getMainNotification(repeatingCountDownTimer.value ?: "..."))
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
        compositeDisposable.clear()
    }

    private fun destroyService() {
        stopSelf()
        stopForeground(true)
    }

    private fun Long.toMilli(): Long = (this * 60000)

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
        NotificationManagerCompat.from(this).notify(238, showFoundPostNotification(postData, newPostCounter))
    }
}

private var running = false