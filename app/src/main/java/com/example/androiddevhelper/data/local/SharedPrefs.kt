package com.example.androiddevhelper.data.local

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import com.example.androiddevhelper.R
import com.example.androiddevhelper.utils.CHANNEL_ID
import com.example.androiddevhelper.utils.CUSTOM_GROUP_ID
import javax.inject.Inject


class SharedPrefs @Inject constructor(
    private val context: Context,
    private val sharedPrefs: SharedPreferences
) {


    fun vibrate(): Boolean =
        sharedPrefs.getBoolean("vibrate", true)

    fun sound(): Boolean =
        sharedPrefs.getBoolean("sound", true)


    /*
    Notification given to any new post, behavior depends on user selected settings.
    The user can: disable/enable the vibration, sound, and heads up notification.
     */
    fun getNewPostNotification(title: String, description: String, postContentIntent: PendingIntent): Notification {

        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.android_icon)
            setContentTitle(title)
            setContentText(description)
            setContentIntent(postContentIntent)
            setGroup(CUSTOM_GROUP_ID)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_HIGH

            if (vibrate()) setDefaults(NotificationCompat.DEFAULT_VIBRATE)

            if (sound()) setDefaults(NotificationCompat.DEFAULT_SOUND)

        }.build()
    }
}