package com.example.androiddevhelper.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat

/*
Higher order static factory functions that accepts a ViewModel and returns it's lazy reference.
 */

inline fun <reified T : ViewModel> PreferenceFragmentCompat.injectViewModel(
    crossinline provider: () -> T
) = viewModels<T> {
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            provider() as T
    }
}

inline fun <reified T : ViewModel> FragmentActivity.injectViewModel(
    crossinline provider: () -> T
) = viewModels<T> {
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            provider() as T
    }
}


fun openRedditPost(context: Context, api: String) {
    val url = BASE_URL + api
    Intent(
        Intent.ACTION_VIEW, Uri.parse(url)
    ).also { intent -> context.startActivity(intent) }
}