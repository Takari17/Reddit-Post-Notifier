package com.takari.redditpostnotifier.di

import android.content.Context
import com.takari.redditpostnotifier.data.misc.Repository
import com.takari.redditpostnotifier.ui.history.PostHistoryViewModel
import com.takari.redditpostnotifier.ui.common.SharedViewModel
import com.takari.redditpostnotifier.ui.settings.SettingsViewModel
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