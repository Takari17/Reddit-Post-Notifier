package com.takari.redditpostnotifier.di

import android.content.Context
import com.takari.redditpostnotifier.features.reddit.data.Repository
import com.takari.redditpostnotifier.features.reddit.newPostHistory.PostHistoryViewModel
import com.takari.redditpostnotifier.features.reddit.SharedViewModel
import com.takari.redditpostnotifier.features.settings.SettingsViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    val sharedViewModel: SharedViewModel
    val settingsViewModel: SettingsViewModel
    val postHistoryViewModel: PostHistoryViewModel
    val repository: Repository

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}