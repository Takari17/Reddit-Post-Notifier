package com.example.androiddevhelper.feature.postdata.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.androiddevhelper.App.Companion.applicationComponent
import com.example.androiddevhelper.feature.postdata.data.remote.Post
import com.example.androiddevhelper.logD
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class PostDataService : Service() {

    private val repo = applicationComponent().repository
    private val subReddit = repo.getSavedSubRedditName("doggos")
    private val notification = applicationComponent().postDataNotifications
    private val viewedPost: MutableList<Post> = mutableListOf()
    private val compositeDisposable = CompositeDisposable()


    override fun onCreate() {
        super.onCreate()
        running = true

        /*
        With every api call reddit returns the top 24 reddit post regardless of whether or not
        the user has seen them already. So I created this hacky solution to ultimately allow
        the user to only get notified of post from the point at which they start listening.

        Fyi future me, distinctUntilChange doesn't fit our use case.
         */
        compositeDisposable += repo.executeGetNewPost(subReddit)
            .subscribeOn(Schedulers.io())
            .map { it.data.children }
            .subscribeBy(
                onNext = { postList -> postList.forEach { post -> viewedPost.add(post) } },
                onError = { logD("Error making initial api call in PostDataService: $it") }
            )


        compositeDisposable += Observable.interval(3, TimeUnit.MINUTES)
            .subscribeOn(Schedulers.io())
            .concatMap {
                repo.executeGetNewPost(subReddit)
                    .map { response -> response.data.children }
                    .concatMap { postList ->
                        Observable.fromIterable(postList)
                            .filter { post -> post !in viewedPost }
                            .zipWith(Observable.interval(3, TimeUnit.SECONDS))
                            .map { it.first }
                    }
            }
            .subscribeBy(
                onNext = { newPost ->
                    notification.create(newPost.data, postPendingIntent(newPost.data.sourceUrl))

                    repo.insertPostData(newPost.data)
                        .subscribeBy(onError = { logD("Error inserting post data in PostDataService") })

                    viewedPost.add(newPost)
                },
                onError = { logD("Error observing for new post in PostDataService: $it") }
            )
    }


    override fun onBind(intent: Intent?): IBinder? = null

    /*
    We're gonna have to add some logic to our broadcast receiver as well, it's intent action
    will be different for when we want to reset or open a reddit post.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == RESET) {
            repo.reset.accept(Unit)
            destroyService()
            return START_NOT_STICKY
        }

        startForeground(100, notification.getMain(resetPendingIntent()))
        return START_STICKY
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


    private fun resetPendingIntent(): PendingIntent {
        val receiverIntent = Intent(this, PostDataReceiver::class.java).apply {
            action = RESET
        }
        return PendingIntent.getBroadcast(this, 2, receiverIntent, 0)
    }


    private fun postPendingIntent(api: String): PendingIntent {
        val receiverIntent = Intent(this, PostDataReceiver::class.java).apply {
            action = SHOW_POST
            putExtra(SOURCE_URL, api)
        }
        return PendingIntent.getBroadcast(this, 0, receiverIntent, 0)
    }

    companion object {
        fun getIsRunning() = running

        const val SUB_NAME = "sub reddit name"
        const val RESET = "reset"
        const val SHOW_POST = "show post"
        const val SOURCE_URL = "source url"
    }
}

private var running = false