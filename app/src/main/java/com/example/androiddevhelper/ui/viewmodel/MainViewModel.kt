package com.example.androiddevhelper.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androiddevhelper.data.Repository
import com.example.androiddevhelper.data.local.PostData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject


class MainViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val newPostDataList = MutableLiveData<List<PostData>>()

    init {
        observeAllPostData()
    }

    fun getNewPostDataList(): LiveData<List<PostData>> = newPostDataList

    private fun observeAllPostData() {
        compositeDisposable += repository.observeAllPostData()
            .subscribeBy(
                onNext = { postDataList -> newPostDataList.postValue(postDataList) },
                onError = { Log.d("zwi", "Error observing all post data from local db") }
            )
    }

    fun deleteItem(titleString: String) = repository.deleteItem(titleString)

    fun startService() = repository.startService()

    fun resetService() = repository.resetService()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        repository.clearCompositeDisposable()
    }
}