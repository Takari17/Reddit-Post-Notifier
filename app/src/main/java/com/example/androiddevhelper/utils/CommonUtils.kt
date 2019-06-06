package com.example.androiddevhelper.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import com.example.androiddevhelper.R

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

fun openRedditPostWithToast(context: Context, api: String) {
    val url = BASE_URL + api
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
    Toast.makeText(context, R.string.opening_post, Toast.LENGTH_SHORT).show()
}

fun getResourceString(context: Context, id: Int): String = context.resources.getString(id)

fun getUserString(context: Context, id: Int, user: String): String =  context.resources.getString(id, user)
