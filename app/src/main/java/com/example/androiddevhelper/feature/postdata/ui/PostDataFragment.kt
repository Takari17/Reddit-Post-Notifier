package com.example.androiddevhelper.feature.postdata.ui

import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androiddevhelper.App.Companion.applicationComponent
import com.example.androiddevhelper.R
import com.example.androiddevhelper.feature.postdata.events.PostDataEvents
import com.example.androiddevhelper.feature.postdata.events.PostDataSingleEvent
import com.example.androiddevhelper.feature.postdata.events.PostDataUIEvent
import com.example.androiddevhelper.feature.postdata.service.PostDataService
import com.example.androiddevhelper.feature.postdata.service.PostDataService.Companion.SUB_NAME
import com.example.androiddevhelper.injectViewModel
import com.example.androiddevhelper.logD
import com.example.androiddevhelper.openRedditPost
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.initial_fragment.*


class PostDataFragment : Fragment() {

    private val viewModel: PostDataEvents by injectViewModel { applicationComponent().postDataViewModel }
    private lateinit var postDataAdapter: PostDataAdapter
    private val examplePrompt = ExamplePrompt()
    private val serviceIntent by lazy { Intent(context, PostDataService::class.java) }
    private val compositeDisposable = CompositeDisposable()
    private val initialConstraints = ConstraintSet()
    private val validConstraints = ConstraintSet()
    private val listeningConstraints = ConstraintSet()
    private val defaultTransition = ChangeBounds().apply {
        interpolator = AnticipateOvershootInterpolator(.5f)
        duration = 1800
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.initial_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postDataAdapter = PostDataAdapter({ postData -> //onClick
            viewModel.onViewEvent(PostDataUIEvent.AdapterItemClick(postData))
        }, { postData -> //onSwipe
            viewModel.onViewEvent(PostDataUIEvent.AdapterSwipe(postData))
        })

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = postDataAdapter
            postDataAdapter.attachOnSwipe(this)
        }


        validateSubButton.setOnClickListener {
            val subReddit: String = subRedditNamr.text.toString()
            if (subReddit.isNotEmpty())
                viewModel.onViewEvent(PostDataUIEvent.ValidateSubClick(subReddit))
            else Toast.makeText(context, "Type in a SubReddit", Toast.LENGTH_SHORT).show()
        }

        statusImage.setOnClickListener {
            if (!examplePrompt.isAdded)
                examplePrompt.show(activity!!.supportFragmentManager, "Status Image")
        }

        listenForPostButton.setOnClickListener {
            viewModel.onViewEvent(PostDataUIEvent.ListenButtonClick)
        }

        stopListeningButton.setOnClickListener {
            viewModel.onViewEvent(PostDataUIEvent.StopListeningButtonClick)
        }

        initialConstraints.clone(initialConstraintLayout)

        validConstraints.clone(context, R.layout.valid_fragment)

        listeningConstraints.clone(context, R.layout.listening_fragment)

        compositeDisposable += viewModel.state()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { state ->

                    when (state.isLoading) {
                        true -> overlapProgressBar()
                        false -> hideProgressBar()
                    }

                    when (state.subRedditStatus) {
                        SubRedditStatus.Empty -> statusImage.setImageResource(R.drawable.help_icon)
                        SubRedditStatus.Valid -> statusImage.setImageResource(R.drawable.green_check)
                        SubRedditStatus.Invalid -> statusImage.setImageResource(R.drawable.red_x)
                    }

                    postDataAdapter.update(state.postDataList)

                    subNameTextView.text = "Observing r/${state.subReddit}"
                },
                onError = { logD("Error observing postListViewState in PostDataFragment: $it") }
            )

        compositeDisposable += viewModel.singleEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { event ->
                    when (event) {
                        is PostDataSingleEvent.StartValidAnimation -> startValidAnimation()
                        is PostDataSingleEvent.StartListeningAnimation -> startListeningAnimation(
                            Duration.Default
                        )
                        is PostDataSingleEvent.ResetAnimations -> resetAnimation()
                        is PostDataSingleEvent.RestoreAnimation -> startListeningAnimation(Duration.Instant)
                        is PostDataSingleEvent.StartService -> startService()
                        is PostDataSingleEvent.ResetService -> resetService()
                        is PostDataSingleEvent.OpenRedditPost -> context!!.openRedditPost(event.sourceUrl)
                    }
                },
                onError = { logD("Error observing singleEvent in PostDataFragment: $it") }
            )

        viewModel.onViewEvent(PostDataUIEvent.OnCreateFinish)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }


    private fun startValidAnimation() {
        TransitionManager.beginDelayedTransition(initialConstraintLayout, defaultTransition)
        validConstraints.applyTo(initialConstraintLayout)
    }


    private fun startListeningAnimation(duration: Duration) {
        when (duration) {
            Duration.Default -> {
                TransitionManager.beginDelayedTransition(initialConstraintLayout, defaultTransition)
            }
            Duration.Instant -> {
                val customTransition = ChangeBounds().apply {
                    interpolator = AnticipateOvershootInterpolator(1.5f)
                    this.duration = 0
                }
                TransitionManager.beginDelayedTransition(initialConstraintLayout, customTransition)
            }
        }
        listeningConstraints.applyTo(initialConstraintLayout)
    }


    private fun resetAnimation() {
        TransitionManager.beginDelayedTransition(initialConstraintLayout, defaultTransition)
        initialConstraints.applyTo(initialConstraintLayout)
    }

    private fun overlapProgressBar() {
        statusImage.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        statusImage.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
    }

    private fun startService() {
        val subReddit: String = subRedditNamr.text.toString()
        serviceIntent.putExtra(SUB_NAME, subReddit)
        context!!.startService(serviceIntent)
    }

    private fun resetService() {
        context!!.stopService(serviceIntent)
    }

    enum class SubRedditStatus {
        Empty, Valid, Invalid
    }

    private enum class Duration {
        Instant, Default
    }
}
