package com.example.androiddevhelper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat

/*
Higher order static factory functions that accepts a ViewModel and returns it's lazy reference.
 */

inline fun <reified T : ViewModel> Fragment.injectViewModel(
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

//todo refactor
fun openRedditPostWithToast(context: Context, api: String) {
    val url = BASE_URL + api
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
    Toast.makeText(context, R.string.opening_post, Toast.LENGTH_SHORT).show()
}

fun logD(message: String) = Log.d("zwi", message)

