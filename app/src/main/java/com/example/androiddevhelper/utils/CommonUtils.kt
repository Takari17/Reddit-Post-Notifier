package com.example.androiddevhelper.utils

import android.content.Context
import android.content.Intent
import android.net.Uri


fun openRedditPost(context: Context, api: String) {
    val url = BASE_URL + api

    Intent(
        Intent.ACTION_VIEW, Uri.parse(url)
    ).also { intent -> context.startActivity(intent) }
}