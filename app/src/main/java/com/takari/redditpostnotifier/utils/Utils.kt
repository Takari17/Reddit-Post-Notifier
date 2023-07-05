package com.takari.redditpostnotifier.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.takari.redditpostnotifier.features.reddit.data.RedditApi
import com.takari.redditpostnotifier.features.reddit.newPost.models.PostData

//todo this doesn't need to be in a utils file
fun Context.openRedditPost(sourceUrl: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    this.startActivity(intent)
}

fun prependBaseUrlIfCrossPost(postData: PostData) =
    if (postData.crossPostParentName != null) "${RedditApi.BASE_URL}${postData.sourceUrl}"
    else postData.sourceUrl

fun logD(message: String?) = Log.d("zwi", message ?: "")