package com.example.androiddevhelper.injection

import android.content.Context
import com.example.androiddevhelper.feature.Repository
import com.example.androiddevhelper.feature.postdata.service.PostDataNotifications
import com.example.androiddevhelper.feature.postdata.ui.PostDataViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    val postDataViewModel: PostDataViewModel
    val repository: Repository
    val postDataNotifications: PostDataNotifications

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}