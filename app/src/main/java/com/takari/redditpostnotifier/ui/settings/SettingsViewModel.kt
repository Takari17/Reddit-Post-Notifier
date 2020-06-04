package com.takari.redditpostnotifier.ui.settings

import androidx.lifecycle.ViewModel
import com.takari.redditpostnotifier.data.misc.Repository
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    fun getIntFromSharedPrefs(key: String, defaultValue: Int) =
        repository.getIntFromSharedPrefs(key, defaultValue)

    fun saveIntToSharedPrefs(key: String, value: Int) {
        repository.saveIntToSharedPrefs(key, value)
    }
}