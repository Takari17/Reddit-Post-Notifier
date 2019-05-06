package com.example.androiddevhelper.data

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.androiddevhelper.R
import com.example.androiddevhelper.data.remote.reddit.RedditApi
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost
import com.example.androiddevhelper.ui.MainActivity
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


const val NOTIFICATION_ID = 17
const val CHANNEL_ID = "My channel"
const val RESET_ACTION = "reset service"

class MyService : Service() {

    private val compositeDisposable = CompositeDisposable()

    private val binder = LocalBinder()

    val interval = Observable.interval(1, TimeUnit.MINUTES)
        .map { getAllNewRedditPost()
        Log.d("zwi", "Network call emitted")} // make network call here

    val networkCallData = PublishRelay.create<List<NewRedditPost>>()


    private val redditApi = RedditApi.invoke()

    private fun executeNetworkCall() = redditApi.getAllPostData()

    private fun getAllNewRedditPost() {
        compositeDisposable += executeNetworkCall()
            .subscribeOn(Schedulers.io())
            .map { it.data.children }
            .subscribeBy(
                onSuccess = { list -> networkCallData.accept(list) },
                onError = {}
            )
    }


    override fun onCreate() {
        super.onCreate()
        interval.subscribe()
        Log.d("zwi", "Subbed")
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        Log.d("zwi", "Cleared")
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        /*
        We're gonna have a reset button, thats it, so do

        if intent.action == ACTION_RESET
         */

        val activityIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            this,
            0, activityIntent, 0
        )

        val resetIntent = getBroadcastReceiverIntent()
        resetIntent.action = RESET_ACTION
        val pendingResetIntent = createPendingIntent(resetIntent)


        startForeground(NOTIFICATION_ID, getMyNotification(contentIntent, pendingResetIntent))
        return START_NOT_STICKY
    }

    private fun getMyNotification(contentIntent: PendingIntent, pendingResetIntent: PendingIntent): Notification {

        return NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.small_icon)
//            addAction(R.drawable.reset_icon, "Reset", pendingResetIntent)
            setSubText("This is the Sub Text")
            setContentTitle("This is the Title")
            setContentText("This is the Content Text")
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(NotificationCompat.CATEGORY_MESSAGE)
            color = Color.BLACK
            setContentIntent(contentIntent)
            setOnlyAlertOnce(true)
        }.build()
    }

    private fun resetAll() {
//        resetBooleans()
//        if (!isAppInForeground) terminateAll() // will terminate app if it's in the background
//        stopSelf()
//        stopForeground(true)
    }

    private fun createPendingIntent(intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(this, 0, intent, 0)
    }

    private fun getBroadcastReceiverIntent(): Intent {
        return Intent(this, NotificationBroadcastReceiver::class.java)
    }

    private fun terminateAll() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    inner class LocalBinder : Binder() {
        fun getService(): MyService = this@MyService
    }

    // Receives click events from the foreground notification button
    inner class NotificationBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val actionIntent = Intent(context, MyService::class.java)


        }
    }
}