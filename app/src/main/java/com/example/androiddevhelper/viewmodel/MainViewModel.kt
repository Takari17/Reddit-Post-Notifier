package com.example.androiddevhelper.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.androiddevhelper.data.Repository
import com.example.androiddevhelper.data.local.DbData
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost
import com.example.androiddevhelper.data.remote.reddit.response.PostData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/*
Things To-Do:

todo style the UI

todo we're getting some slow performance, I think we're performing some firestore stuff on the main thread, look into that

todo should probably fix that recycler view bug

todo thinkin of displaying a "No Post Found Yet!" text if my recycler view is empty, probably do the logic for that in this vm  (use mediator live data)
 */

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application)
    private val compositeDisposable = CompositeDisposable()

    /*
    Will only emit values if the list is not empty, fixed our recycler view bug
    (can't create a recycler view with no values)
     */
    val newRedditPostList = MediatorLiveData<List<NewRedditPost>>()

    init {
        newRedditPostList.addSource(getNewRedditPostList()) { list ->
            if (list.isEmpty()) {
                val emptyDefaultMessage = listOf(NewRedditPost(PostData("", "No Post Found!", "")))
                newRedditPostList.postValue(emptyDefaultMessage)
            }else{
                newRedditPostList.postValue(list)

            }
        }
    }
    private fun getNewRedditPostList(): LiveData<List<NewRedditPost>> = repository.newRedditPostList

    fun startService() = repository.startService()

    fun resetService() = repository.resetService()

    fun bindToService() = repository.bindToService()

    fun unbindFromService() = repository.unbindFromService()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        repository.clearCompositeDisposable()
    }
}