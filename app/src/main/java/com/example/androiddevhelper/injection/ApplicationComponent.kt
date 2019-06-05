package com.example.androiddevhelper.injection

import android.content.Context
import com.example.androiddevhelper.data.Repository
import com.example.androiddevhelper.data.local.SharedPrefs
import com.example.androiddevhelper.data.remote.reddit.RedditApi
import com.example.androiddevhelper.ui.adapters.MyAdapter
import com.example.androiddevhelper.ui.viewmodel.MainViewModel
import com.example.androiddevhelper.ui.viewmodel.SettingsViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    //Dependencies
    val mainViewModel: MainViewModel
    val settingsViewModel: SettingsViewModel
    val repository: Repository
    val myAdapter: MyAdapter

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}