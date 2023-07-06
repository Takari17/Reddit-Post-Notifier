package com.takari.redditpostnotifier.features.settings

import androidx.lifecycle.ViewModel
import com.takari.redditpostnotifier.features.reddit.data.RedditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: RedditRepository
) : ViewModel() {

    fun getApiRequestRate() = repository.getApiRequestRate()

    fun saveApiRequestRate(value: Int) {
        repository.saveApiRequestRate(value)
    }
}