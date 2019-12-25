package com.example.androiddevhelper.feature.postdata.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.androiddevhelper.CHANNEL_ID
import com.example.androiddevhelper.R
import com.example.androiddevhelper.feature.MainActivity
import com.example.androiddevhelper.feature.postdata.data.local.PostData
import javax.inject.Inject


class PostDataNotifications @Inject constructor(
    private val context: Context,
    private var id: Int
) {

    fun getMain(resetPendingIntent: PendingIntent): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.white_android_icon)
            setContentTitle("Android Dev Helper")
            setContentText("Listening For New Reddit Post…")
            addAction(R.drawable.reset, "Reset", resetPendingIntent)
            setContentIntent(activityIntent())
        }.build()


    private fun newNotification(newPost: PostData, pendingIntent: PendingIntent): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.white_android_icon)
            setContentTitle("${newPost.author} posted:")
            setContentText(newPost.title)
            setContentIntent(pendingIntent)
            setGroup(GROUP_ID)
            setAutoCancel(true)
        }.build()


    private fun summaryNotification() =
        NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.white_android_icon)
            setGroupSummary(true)
            setGroup(GROUP_ID)
            setStyle(NotificationCompat.InboxStyle().setSummaryText("Found New Post"))
            setContentIntent(activityIntent())
            setGroup(GROUP_ID)
        }.build()


    fun create(newPost: PostData, pendingIntent: PendingIntent): NotificationManagerCompat =
        NotificationManagerCompat.from(context).apply {
            id++
            notify(id, newNotification(newPost, pendingIntent))
            notify(SUMMARY_ID, summaryNotification())
        }


    private fun activityIntent(): PendingIntent? {
        val intent = MainActivity.createIntent(context)

        return PendingIntent.getActivity(context, 0, intent, 0)
    }


}

private const val GROUP_ID = "My own custom group :D"
private const val SUMMARY_ID = 120120
