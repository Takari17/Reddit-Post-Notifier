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

class PostDataViewModel @Inject constructor(private val repo: Repository) : ViewModel(),
    PostDataEvents {

    private val compositeDisposable = CompositeDisposable()
    private val view = PublishRelay.create<PostDataSingleEvent>()
    private val state = BehaviorRelay.createDefault(
        PostDataUIState(
            subRedditStatus = PostDataFragment.SubRedditStatus.Empty,
            isLoading = false,
            postDataList = emptyList(),
            subReddit = ""
        )
    )


    init {
        compositeDisposable += repo.listenToPostData()
            .subscribeBy(
                onNext = { newPostList -> state.updatePostDataList(newPostList) },
                onError = { logD("Error observing new post in PostDataViewModel $it") }
            )

        compositeDisposable += repo.reset
            .subscribeBy {
                //some duplication here but it's whatever
                view.accept(PostDataSingleEvent.ResetAnimations)
                repo.deleteAllPostData()
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            }
    }


    override fun onViewEvent(event: PostDataUIEvent) {
        when (event) {
            is PostDataUIEvent.OnCreateFinish -> {
                if (repo.isServiceRunning()) {
                    view.accept(PostDataSingleEvent.RestoreAnimation)
                    state.updateSubReddit(repo.getSavedSubRedditName("error ;-;)/"))
                }
            }

            is PostDataUIEvent.AdapterItemClick -> {
                repo.deletePostData(event.postTitle)
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            }

            is PostDataUIEvent.ValidateSubClick -> {
                repo.validateSubReddit(event.subReddit)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { state.updateIsLoading(true) }
                    .doOnTerminate { state.updateIsLoading(false) }
                    .subscribeBy(
                        onSuccess = {
                            state.updateSubRedditStatus(PostDataFragment.SubRedditStatus.Valid)
                            state.updateSubReddit(event.subReddit)
                            repo.saveSubRedditName(event.subReddit)
                            view.accept(PostDataSingleEvent.StartValidAnimation)
                        },
                        onError = {
                            logD("Error validating subReddit in PostDataViewModel: $it")
                            state.updateSubRedditStatus(PostDataFragment.SubRedditStatus.Invalid)
                        }
                    )
            }
            is PostDataUIEvent.AdapterSwipe -> repo.deletePostData(event.title)

            is PostDataUIEvent.ListenButtonClick -> {
                view.accept(PostDataSingleEvent.StartService)
                view.accept(PostDataSingleEvent.StartListeningAnimation)
            }

            is PostDataUIEvent.StopListeningButtonClick -> {
                view.accept(PostDataSingleEvent.ResetService)
                view.accept(PostDataSingleEvent.ResetAnimations)
                repo.deleteAllPostData()
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            }
        }
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