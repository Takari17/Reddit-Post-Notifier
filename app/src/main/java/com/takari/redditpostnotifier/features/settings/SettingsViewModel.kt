package com.takari.redditpostnotifier.features.settings

import androidx.lifecycle.ViewModel
import com.takari.redditpostnotifier.features.reddit.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    fun getIntFromSharedPrefs(key: String, defaultValue: Int) = //todo this should be generic (get_int)
        repository.getIntFromSharedPrefs(key, defaultValue)

    fun saveIntToSharedPrefs(key: String, value: Int) {
        repository.saveIntToSharedPrefs(key, value)
    }
}