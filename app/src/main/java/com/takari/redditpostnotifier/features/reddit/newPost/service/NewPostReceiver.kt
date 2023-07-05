package com.takari.redditpostnotifier.features.reddit.newPost.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.takari.redditpostnotifier.features.reddit.newPost.service.NewPostService.Companion.RESET

class NewPostReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == RESET) triggerServiceReset(context)
    }

    private fun triggerServiceReset(context: Context) {
        Intent(context, NewPostService::class.java).also { serviceIntent ->
            serviceIntent.action = RESET
            context.startService(serviceIntent)
        }
    }
}
