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

//Allows dagger to inject ViewModels with args.
inline fun <reified T : ViewModel> Fragment.injectViewModel(
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

fun logD(message: String) = Log.d("zwi", message)

