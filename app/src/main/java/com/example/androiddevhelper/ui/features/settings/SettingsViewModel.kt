package com.example.androiddevhelper.ui.features.settings

import androidx.lifecycle.ViewModel
import com.example.androiddevhelper.data.local.SharedPrefs
import javax.inject.Inject

// Wanted to avoid holding a shared preference reference in the UI
class SettingsViewModel @Inject constructor(val sharedPrefs: SharedPrefs) : ViewModel() {

    //Forever alone

}