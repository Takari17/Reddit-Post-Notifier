package com.takari.redditpostnotifier.ui.subreddit.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.animation.doOnEnd
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.ui.common.SharedViewModel.Companion.LIMIT_REACHED
import com.takari.redditpostnotifier.ui.common.SharedViewModel.Companion.NO_CONNECTION
import com.takari.redditpostnotifier.misc.injectViewModel
import com.takari.redditpostnotifier.misc.ResponseState
import com.takari.redditpostnotifier.ui.common.SharedViewModel
import com.jakewharton.rxbinding3.view.clicks
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import mehdi.sakout.fancybuttons.FancyButton
import java.util.concurrent.TimeUnit


class SubRedditValidationDialog : AppCompatDialogFragment() { 

    private val viewModel: SharedViewModel by injectViewModel { App.applicationComponent().sharedViewModel } // do we need to scope our dependencies?
    private val compositeDisposable = CompositeDisposable()
    private lateinit var successAnim: LottieAnimationView
    private lateinit var errorAnim: LottieAnimationView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).apply {
            val view = requireActivity().layoutInflater.inflate(R.layout.validation_dialog_layout, null)
            setView(view)

            //Kotlin synthetics failed me ;(
            val validationButton = view.findViewById<FancyButton>(R.id.validationButton)
            val finishedButton = view.findViewById<Button>(R.id.finishedButton)
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar2)
            val subRedditName = view.findViewById<TextInputLayout>(R.id.subRedditName)
            successAnim = view.findViewById(R.id.successAnim)
            errorAnim = view.findViewById(R.id.errorAnim)

            successAnim.addAnimatorUpdateListener { valueAnimator ->
                valueAnimator.doOnEnd { successAnim.visibility = View.INVISIBLE }
            }

            errorAnim.addAnimatorUpdateListener { valueAnimator ->
                valueAnimator.doOnEnd { errorAnim.visibility = View.INVISIBLE }
            }

            subRedditName.editText!!.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    //on keyboard "enter" key click
                    validationButton.performClick()
                }
                false
            }

            finishedButton.setOnClickListener { dismiss() }

            compositeDisposable += validationButton.clicks()
                .throttleFirst(2, TimeUnit.SECONDS)
                .doOnEach { progressBar.show() }
                .flatMap {
                    viewModel.getSubRedditData(subRedditName.editText!!.text.toString())
                        .subscribeOn(Schedulers.io()) // the initial observable is a view so it cant sub on a background thread, only the flatmap's can
                        .toObservable()
                }
                .flatMap { result ->
                    if (result is ResponseState.Success) {
                        viewModel.insertDbSubRedditDataWithCap(result.item!!)
                            .toObservable()
                            .subscribeOn(Schedulers.io())
                    } else Observable.just(result)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy { result -> //onError won't be invoked

                    progressBar.hide()

                    when (result) {
                        is ResponseState.Success -> {
                            showSuccessAnimation()
                            showSuccessToast(context)
                        }
                        is ResponseState.Error -> {
                            showErrorAnimation()
                            showErrorToast(context, result.eMsg)
                        }
                    }

                }
        }.create()

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun ProgressBar.hide() {
        this.visibility = View.INVISIBLE
    }

    private fun ProgressBar.show() {
        this.visibility = View.VISIBLE
    }

    private fun showSuccessAnimation() {
        //anim won't start unless the view is visible
        successAnim.apply {
            speed = .5f
            visibility = View.VISIBLE
            playAnimation()
        }
    }

    private fun showErrorAnimation() {
        errorAnim.apply {
            speed = .5f
            visibility = View.VISIBLE
            playAnimation()
        }
    }

    private fun showSuccessToast(context: Context) {
        Toasty.success(context, "Added", Toasty.LENGTH_SHORT, true).show()
    }

    private fun showErrorToast(context: Context, msg: String) {
        val errorMsg = when (msg) {
            NO_CONNECTION -> "No Connection"
            LIMIT_REACHED -> "SubReddit Limit Reached (12 Max)"
            else -> "Not Found"
        }

        Toasty.error(context, errorMsg, Toasty.LENGTH_SHORT, true).show()
    }
}
