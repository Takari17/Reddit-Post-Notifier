package com.takari.redditpostnotifier.ui.post.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.data.misc.RedditApi
import com.takari.redditpostnotifier.data.subreddit.SubRedditData
import com.takari.redditpostnotifier.misc.injectViewModel
import com.takari.redditpostnotifier.misc.logD
import com.takari.redditpostnotifier.misc.openRedditPost
import com.takari.redditpostnotifier.misc.prependBaseUrlIfCrossPost
import com.takari.redditpostnotifier.ui.common.SharedViewModel
import com.takari.redditpostnotifier.ui.history.NewPostAdapter
import com.takari.redditpostnotifier.ui.post.service.NewPostService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_observing.*


class NewPostFragment : Fragment() {

    private val viewModel: SharedViewModel by injectViewModel { App.applicationComponent().sharedViewModel } // do we need to scope our dependencies?
    private val subIconAdapter = ChosenSubRedditAdapter()
    private val serviceIntent by lazy { Intent(context, NewPostService::class.java) }
    private val compositeDisposable = CompositeDisposable()
    private lateinit var newPostAdapter: NewPostAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_observing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stopObservingButton.setOnClickListener {
            viewModel.switchContainers(SharedViewModel.FragmentName.SubRedditFragment)
            requireContext().stopService(serviceIntent)
        }

        newPostAdapter = NewPostAdapter { clickedPostData ->

            val url = prependBaseUrlIfCrossPost(clickedPostData)

            requireContext().openRedditPost(url)

            compositeDisposable += viewModel.deleteDbPostData(clickedPostData)
                .subscribeOn(Schedulers.io())
                .subscribeBy(onError = { e -> logD("Error deletingDbPostData in PostHistoryActivity: $e") })
        }


        subRedditIconRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = subIconAdapter
        }

        postHistoryRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = newPostAdapter
            swipeHandler.attachToRecyclerView(this)
        }
    }

    override fun onStart() {
        super.onStart()

        if (!NewPostService.isRunning()) {
            serviceIntent.putParcelableArrayListExtra(
                SubRedditData.SUB_REDDIT_DATA_NAME,
                ArrayList(viewModel.subRedditDataList.value!!)
            )
            requireContext().startService(serviceIntent)
            requireContext().bindService(serviceIntent, serviceConnection, 0)
        } else {
            requireContext().bindService(serviceIntent, serviceConnection, 0)
        }

        compositeDisposable += viewModel.listenToDbSubRedditData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { subRedditData -> subIconAdapter.submitList(subRedditData) },
                onError = { e -> logD("Error listeningToDbData in NewPostFragment: $e") }
            )

        compositeDisposable += viewModel.listenToDbPostData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { postDataList -> newPostAdapter.submitList(postDataList) },
                onError = { e -> logD("Error listeningToDbPostData in NewPostFragment: $e") }
            )
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
        requireContext().unbindService(serviceConnection)
    }


    private val swipeHandler = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT //Only can swipe right or left
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false //Don't need this callback

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val swipedPostData = newPostAdapter.getPostData(viewHolder.adapterPosition)
            swipedPostData?.let {
                compositeDisposable += viewModel.deleteDbPostData(swipedPostData)
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(onError = { e -> logD("Error deletingDbPostData in PostHistoryActivity: $e") })
            }
        }
    })


    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val postDataService = (service as NewPostService.LocalBinder).getService()

            compositeDisposable += postDataService.repeatingCountDownTimer
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { timeInSeconds ->
                        timerTextView.text = "Connecting In: $timeInSeconds secs"
                    },
                    onError = { e -> logD("Error observing repeatingCountDownTimer in NewPostFragment: $e") }
                )

            compositeDisposable += postDataService.reset
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { viewModel.switchContainers(SharedViewModel.FragmentName.SubRedditFragment) },
                    onError = { e -> logD("Error observing postDataService.reset in NewPostFragment: $e") }
                )
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }

}
