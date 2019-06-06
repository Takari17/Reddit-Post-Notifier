package com.example.androiddevhelper.ui.features.postlist

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


class PostListViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val newPostDataList = MutableLiveData<List<PostData>>()

    // Observes the data from the local db(Room) and updates the Live Data when there's any change
    init {
        compositeDisposable += repository.observeAllPostData()
            .subscribeBy(
                onNext = { postDataList -> newPostDataList.postValue(postDataList) },
                onError = { Log.d("zwi", "Error observing all post data from local db: $it") }
            )
    }

    fun startService() = repository.startService()

    fun resetService() = repository.resetService()

    fun getNewPostDataList(): LiveData<List<PostData>> = newPostDataList

    fun deleteItem(titleString: String) = repository.deleteItem(titleString)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}