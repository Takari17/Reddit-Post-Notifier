package com.takari.redditpostnotifier.ui.subreddit.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.misc.injectViewModel
import com.takari.redditpostnotifier.misc.logD
import com.takari.redditpostnotifier.ui.common.SharedViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_post_data.*

class SubRedditFragment : Fragment() {

    private val viewModel: SharedViewModel by injectViewModel { App.applicationComponent().sharedViewModel }
    private val validationDialog = SubRedditValidationDialog()
    private val compositeDisposable = CompositeDisposable()
    private val handler = Handler()

    //espresso kept giving a null error with this specific view for whatever reason unless I used findViewById
    private lateinit var queuedSubRedditRecyclerView: RecyclerView

    private val queuedSubredditsAdapter =
        QueuedSubredditsAdapter { clickedSubData ->
            viewModel.deleteDbSubRedditData(clickedSubData)
                .subscribeOn(Schedulers.io())
                .subscribe()
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeButton.setOnClickListener {
            viewModel.switchContainers(SharedViewModel.FragmentName.NewPostFragment)
        }

        addSubRedditFab.setOnClickListener {
            if (!validationDialog.isAdded)
                validationDialog.show(
                    activity?.supportFragmentManager!!,
                    "SubRedditValidationDialog"
                )
        }

        queuedSubRedditRecyclerView = view.findViewById(R.id.queuedSubRedditRecyclerView)

        queuedSubRedditRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = queuedSubredditsAdapter
            queuedSubredditsAdapter.attachOnSwipe(this)
        }
    }

    override fun onStart() {
        super.onStart()

        compositeDisposable += viewModel.listenToDbSubRedditData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { subRedditDataList ->

                    queuedSubredditsAdapter.submitList(subRedditDataList.reversed())

                    //without a delay the reycler view wouldn't scroll to the new item for whatever reason
                    handler.postDelayed({
                        queuedSubRedditRecyclerView.smoothScrollToPosition(0)
                    }, 500)

                    //disabled if there's no data in the db
                    observeButton.isEnabled = subRedditDataList.isNotEmpty()

                    //will be received by NewPostFragment
                    viewModel.subRedditDataList.accept(subRedditDataList)
                },
                onError = { e -> logD("Error listening to subRedditData in SubRedditFragment: $e") }
            )
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }
}