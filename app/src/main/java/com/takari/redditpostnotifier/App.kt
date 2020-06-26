package com.takari.redditpostnotifier

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.takari.redditpostnotifier.di.ApplicationComponent
import com.takari.redditpostnotifier.di.DaggerApplicationComponent


class App : Application() {

    companion object {
        //Exposed globally for DI in Android Component classes
        fun applicationComponent() = component
        const val CHANNEL_ID = "custom channel Id"
    }


    override fun onCreate() {
        super.onCreate()

        component = DaggerApplicationComponent
            .factory()
            .create(applicationContext)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // if API >= 26
            val name = "Notification Settings"
            val descriptionText = "Settings for the notifications of this app."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

private lateinit var component: ApplicationComponent