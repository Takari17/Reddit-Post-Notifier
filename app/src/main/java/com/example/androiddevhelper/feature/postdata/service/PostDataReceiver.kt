package com.example.androiddevhelper.feature.postdata.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.androiddevhelper.feature.postdata.service.PostDataService.Companion.RESET
import com.example.androiddevhelper.feature.postdata.service.PostDataService.Companion.SHOW_POST
import com.example.androiddevhelper.feature.postdata.service.PostDataService.Companion.SOURCE_URL
import com.example.androiddevhelper.openRedditPost

class PostDataReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {

            RESET -> triggerServiceReset(context)

            SHOW_POST -> {
                val sourceUrl: String? = intent.getStringExtra(SOURCE_URL)
                context.openRedditPost(sourceUrl ?: "https://www.youtube.com/watch?v=dQw4w9WgXcQ&feature=youtu.be")
            }
        }
    }

    private fun triggerServiceReset(context: Context) {
        Intent(context, PostDataService::class.java).also { serviceIntent ->
            serviceIntent.action = RESET
            context.startService(serviceIntent)
        }
    }
}
