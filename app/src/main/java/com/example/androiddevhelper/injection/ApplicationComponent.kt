package com.example.androiddevhelper.injection

import android.content.Context
import com.example.androiddevhelper.data.Repository
import com.example.androiddevhelper.ui.features.postlist.PostListViewModel
import com.example.androiddevhelper.ui.features.settings.SettingsViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    //Dependencies
    val postListViewModel: PostListViewModel
    val settingsViewModel: SettingsViewModel
    val repository: Repository

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}