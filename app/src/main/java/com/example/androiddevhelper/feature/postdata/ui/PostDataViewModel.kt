package com.example.androiddevhelper.feature.postdata.ui

import androidx.lifecycle.ViewModel
import com.example.androiddevhelper.feature.Repository
import com.example.androiddevhelper.feature.postdata.data.local.PostData
import com.example.androiddevhelper.feature.postdata.events.PostDataEvents
import com.example.androiddevhelper.feature.postdata.events.PostDataSingleEvent
import com.example.androiddevhelper.feature.postdata.events.PostDataUIEvent
import com.example.androiddevhelper.logD
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PostDataViewModel @Inject constructor(
    private val repo: Repository,
    private val defaultState: PostDataUIState
) : ViewModel(), PostDataEvents {


    private val view = PublishRelay.create<PostDataSingleEvent>()
    private val state = BehaviorRelay.createDefault(defaultState)
    private val compositeDisposable = CompositeDisposable()


    init {
        compositeDisposable += repo.listenToPostData()
            .subscribeBy(
                onNext = { newPostList -> state.updatePostDataList(newPostList) },
                onError = { logD("Error observing post data in PostDataViewModel $it") }
            )

        compositeDisposable += repo.reset
            .subscribeBy(
                onNext = { resetState() },
                onError = { logD("Error observing reset in PostDataViewModel: $it") }
            )
    }


    override fun onViewEvent(event: PostDataUIEvent) {
        when (event) {
            is PostDataUIEvent.OnCreateFinish ->
                if (repo.isServiceRunning()) restoreViewState()

            is PostDataUIEvent.AdapterItemClick -> {
                openRedditPost(event.postData)
                deletePost(event.postData)
            }

            is PostDataUIEvent.AdapterSwipe ->
                deletePost(event.postData)

            is PostDataUIEvent.ValidateSubClick ->
                validateSubRedditExist(event.subReddit)

            is PostDataUIEvent.ListenButtonClick -> {
                view.accept(PostDataSingleEvent.StartService)
                view.accept(PostDataSingleEvent.StartListeningAnimation)
            }

            is PostDataUIEvent.StopListeningButtonClick -> {
                view.accept(PostDataSingleEvent.ResetService)
                resetState()
            }
        }
    }


    private fun restoreViewState() {
        view.accept(PostDataSingleEvent.RestoreAnimation)
        state.updateSubReddit(repo.getSavedSubRedditName("error ;-;)/"))
    }


    private fun deletePost(postData: PostData) {
        repo.deletePostData(postData)
            .subscribeOn(Schedulers.io())
            .subscribeBy(onError = { logD("Error deleting post data on click in PostDataViewModel: $it") })
    }


    private fun openRedditPost(postData: PostData) {
        view.accept(PostDataSingleEvent.OpenRedditPost(postData.sourceUrl))
    }


    private fun validateSubRedditExist(subReddit: String) {
        repo.validateSubReddit(subReddit)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { state.updateIsLoading(true) }
            .doOnTerminate { state.updateIsLoading(false) }
            .subscribeBy(
                onSuccess = {
                    state.updateSubRedditStatus(PostDataFragment.SubRedditStatus.Valid)
                    state.updateSubReddit(subReddit)
                    repo.saveSubRedditName(subReddit)
                    view.accept(PostDataSingleEvent.StartValidAnimation)
                },
                onError = {
                    state.updateSubRedditStatus(PostDataFragment.SubRedditStatus.Invalid)
                    logD("Error validating subReddit in PostDataViewModel: $it")
                }
            )
    }


    private fun resetState() {
        state.accept(defaultState)
        view.accept(PostDataSingleEvent.ResetAnimations)

        repo.deleteAllPostData()
            .subscribeOn(Schedulers.io())
            .subscribeBy(onError = { logD("Error deleting all post data in PostDataViewModel: $it") })
    }


    private fun BehaviorRelay<PostDataUIState>.updateSubRedditStatus(status: PostDataFragment.SubRedditStatus) {
        this.accept(this.value!!.copy(subRedditStatus = status))
    }


    private fun BehaviorRelay<PostDataUIState>.updateIsLoading(loading: Boolean) {
        this.accept(this.value!!.copy(isLoading = loading))
    }


    private fun BehaviorRelay<PostDataUIState>.updatePostDataList(newPostDataList: List<PostData>) {
        this.accept(this.value!!.copy(postDataList = newPostDataList))
    }


    private fun BehaviorRelay<PostDataUIState>.updateSubReddit(subReddit: String) {
        this.accept(this.value!!.copy(subReddit = subReddit))
    }


    override fun state(): Observable<PostDataUIState> = state

    override fun singleEvent(): Observable<PostDataSingleEvent> = view

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}