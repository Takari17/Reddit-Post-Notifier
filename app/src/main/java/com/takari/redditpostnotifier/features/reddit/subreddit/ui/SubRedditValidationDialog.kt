package com.takari.redditpostnotifier.features.reddit.subreddit.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.animation.doOnEnd
import androidx.lifecycle.viewModelScope
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.databinding.ValidationDialogLayoutBinding
import com.takari.redditpostnotifier.utils.ResponseState
import com.takari.redditpostnotifier.utils.injectViewModel
import com.takari.redditpostnotifier.features.reddit.SharedViewModel
import com.takari.redditpostnotifier.features.reddit.SharedViewModel.Companion.LIMIT_REACHED
import com.takari.redditpostnotifier.features.reddit.SharedViewModel.Companion.NO_CONNECTION
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch


class SubRedditValidationDialog : AppCompatDialogFragment() {

    private val viewModel: SharedViewModel by injectViewModel { App.applicationComponent().sharedViewModel } // do we need to scope our dependencies?
    private lateinit var binding: ValidationDialogLayoutBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).apply {
            binding = ValidationDialogLayoutBinding.inflate(layoutInflater)
            setView(binding.root)

            binding.successAnim.addAnimatorUpdateListener { valueAnimator ->
                valueAnimator.doOnEnd { binding.successAnim.visibility = View.INVISIBLE }
            }

            binding.errorAnim.addAnimatorUpdateListener { valueAnimator ->
                valueAnimator.doOnEnd { binding.errorAnim.visibility = View.INVISIBLE }
            }

            binding.subRedditName.editText!!.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    //on keyboard "enter" key click
                    binding.validationButton.performClick()
                }
                false
            }

            binding.finishedButton.setOnClickListener { dismiss() }

            binding.validationButton.setOnClickListener {
                val subName = binding.subRedditName.editText!!.text.toString()

                viewModel.viewModelScope.launch {
                    viewModel.getAndCacheSubRedditData(subName)
                        .flowOn(Dispatchers.Main)
                        .onStart {
                            binding.progressBar2.show()
                            binding.validationButton.isEnabled = false
                        }
                        .onCompletion {
                            binding.progressBar2.hide()
                            binding.validationButton.isEnabled = true
                        }
                        .collect { response ->
                            when (response) {
                                is ResponseState.Success -> {
                                    showSuccessAnimation()
                                    showSuccessToast(context)
                                }

                                is ResponseState.Error -> {
                                    showErrorAnimation()
                                    showErrorToast(context, response.e.message)
                                }
                            }
                        }
                }
            }
        }.create()


    private fun ProgressBar.hide() {
        this.visibility = View.INVISIBLE
    }

    private fun ProgressBar.show() {
        this.visibility = View.VISIBLE
    }

    private fun showSuccessAnimation() {
        //anim won't start unless the view is visible
        binding.successAnim.apply {
            speed = .5f
            visibility = View.VISIBLE
            playAnimation()
        }
    }

    private fun showErrorAnimation() {
        binding.errorAnim.apply {
            speed = .5f
            visibility = View.VISIBLE
            playAnimation()
        }
    }

    private fun showSuccessToast(context: Context) {
        Toasty.success(context, "Added", Toasty.LENGTH_SHORT, true).show()
    }

    private fun showErrorToast(context: Context, msg: String?) {
        val errorMsg = when (msg) {
            NO_CONNECTION -> "No Connection"
            LIMIT_REACHED -> "SubReddit Limit Reached (12 Max)"
            else -> "Not Found"
        }

        Toasty.error(context, errorMsg, Toasty.LENGTH_SHORT, true).show()
    }
}
