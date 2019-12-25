package com.example.androiddevhelper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.androiddevhelper.injection.ApplicationComponent
import com.example.androiddevhelper.injection.DaggerApplicationComponent

class App : Application() {

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


    //Exposed globally for injecting dependencies in Android Component Classes
    companion object {
        fun applicationComponent() = component
    }

}

private lateinit var component: ApplicationComponent