package com.takari.redditpostnotifier.misc

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.takari.redditpostnotifier.data.misc.RedditApi
import com.takari.redditpostnotifier.data.post.PostData


/**
Returns a lazy view model reference scoped to the Activity of a Fragment.
Used so Dagger can inject ViewModels with args.
 */
inline fun <reified T : ViewModel> Fragment.injectViewModel(
    crossinline provider: () -> T
) = activityViewModels<T> {
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            provider() as T
    }
}

/**
Returns a lazy view model reference to an Activity.
Used so Dagger can inject ViewModels with args.
 */
inline fun <reified T : ViewModel> AppCompatActivity.injectViewModel(
    crossinline provider: () -> T
) = viewModels<T> {
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            provider() as T
    }
}

fun Context.openRedditPost(sourceUrl: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    this.startActivity(intent)
}


fun prependBaseUrlIfCrossPost(postData: PostData) =
    if (postData.crossPostParentName != null) "${RedditApi.BASE_URL}${postData.sourceUrl}"
    else postData.sourceUrl

fun logD(message: String?) = Log.d("zwi", message?:"")