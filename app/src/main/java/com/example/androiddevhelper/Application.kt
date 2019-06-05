package com.example.androiddevhelper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.androiddevhelper.injection.ApplicationComponent
import com.example.androiddevhelper.injection.DaggerApplicationComponent
import com.example.androiddevhelper.utils.CHANNEL_ID

class App : Application() {

    /*
    Exposes a Dagger Component globally for classes that aren't mines (e.g service & activity's) so
    that I can instantiate our dependencies right away since Dagger provides them.
     */
    companion object {
        lateinit var applicationComponent: ApplicationComponent
    }


    //Called right when the whole app starts
    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent
            .factory()
            .create(applicationContext)

        createNotificationChannel()
    }

    //Notification Channel for API's 26 and above
    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}