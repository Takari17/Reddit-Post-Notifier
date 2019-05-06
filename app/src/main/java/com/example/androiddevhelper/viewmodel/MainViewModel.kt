package com.example.androiddevhelper.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.androiddevhelper.data.Repository
import com.example.androiddevhelper.data.local.DbData
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/*
Things To-Do:

todo: We need to find a way to send a push notification when a new post is posted

todo:  We also need a way to make a network call every minute (Rx java interval)

todo: We need to move most of this code into our foreground service, and have a button that will start/Reset the service

todo: add the on snapshot listener, our activity is gonna populate its views through fire store, not the network call directly

todo: we need a way to update existing reddit post documents in our fire store db because right now with every network call we just create duplicates

todo: after we fix out duplication issue we gotta figure out how to programmatically send push notifications when we get new data in fire store

todo: after we got that down we need to put most of our code in a service that will makes a network call every 1 min

todo: then we should probably style the UI a bit

todo: see if you can come up with better names for your db stuff

todo we're getting some slow performence, I think we're performing some firestore stuff on the main thread, look into that
 */

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application)
    private val compositeDisposable = CompositeDisposable()


    fun getNewRedditPostList(): LiveData<List<NewRedditPost>> = repository.newRedditPostList

    /*
    Makes a network call to Reddit's Api and returns a list of 24 new post
     */
//    fun getAllNewRedditPost() {
//        compositeDisposable += repository.getAllNewRedditPost()
//            .subscribeOn(Schedulers.io())
//            .map { it.data.children }
//            .subscribeBy(
//                onSuccess = { redditPostList ->
//                    newRedditPostList.postValue(redditPostList)
//                    savePostToFirestore(redditPostList)
//                },
//                onError = {}
//            )
//    }

    private fun savePostToFirestore(redditPostList: List<NewRedditPost>) {
        repository.saveNewPostToDb(redditPostList)
    }

    /*
   Saves the previous reddit post list on stop
    */
    fun savePreviousRedditPost() = repository.saveListToFireStore()

    /*
    Sets the previous reddit post list on start
     */
    fun setPreviousRedditPost() = repository.setPreviousRedditPost()

    fun startService() = repository.startService()

    fun resetService() = repository.resetService()



    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        repository.clearCompositeDisposable()
    }
}