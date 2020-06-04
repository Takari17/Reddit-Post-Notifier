package com.takari.redditpostnotifier.ui.history

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.misc.injectViewModel
import com.takari.redditpostnotifier.misc.logD
import com.takari.redditpostnotifier.misc.openRedditPost
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_post_history.*


class PostHistoryActivity : AppCompatActivity() {

    private val viewModel: PostHistoryViewModel by injectViewModel { App.applicationComponent().postHistoryViewModel }
    private val compositeDisposable = CompositeDisposable()
    private val confirmationDialog = ConfirmationDialog()
    private val postHistoryAdapter by lazy {
        NewPostAdapter { clickedPostData ->

            this.openRedditPost(clickedPostData.sourceUrl)

            compositeDisposable += viewModel.deleteDbPostData(clickedPostData)
                .subscribeOn(Schedulers.io())
                .subscribeBy(onError = { e -> logD("Error deletingDbPostData in PostHistoryActivity: $e") })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.navigationBarColor = Color.parseColor("#171A23")

        postHistoryRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = postHistoryAdapter
            swipeHandler.attachToRecyclerView(this)
        }

        deleteAllButton.setOnClickListener {
            if (!confirmationDialog.isAdded)
                confirmationDialog.show(supportFragmentManager, "ConfirmationDialog")
        }

        confirmationDialog.onYesSelect = {
            compositeDisposable += viewModel.deleteAllDbPostData()
                .subscribeOn(Schedulers.io())
                .subscribeBy(onError = { e -> logD("Error deletingAllDbPostData in PostHistoryFragment: $e") })
        }
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable += viewModel.listenToDbPostData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { postDataList ->
                    if (postDataList.isNotEmpty()) {
                        postHistoryAdapter.submitList(postDataList)
                        showPostHistoryViews()
                    } else showNothingFoundViews()
                },
                onError = { e -> logD("Error listeningToDbPostData in PostHistoryFragment: $e") }
            )
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    private fun showPostHistoryViews() {
        postHistoryRecyclerView.visibility = View.VISIBLE

        deleteAllButton.visibility = View.VISIBLE
        deleteAllButton.isEnabled = true

        nothingFoundIcon.visibility = View.INVISIBLE
        nothingFoundText.visibility = View.INVISIBLE
    }

    private fun showNothingFoundViews() {
        postHistoryRecyclerView.visibility = View.INVISIBLE

        deleteAllButton.visibility = View.INVISIBLE
        deleteAllButton.isEnabled = false

        nothingFoundIcon.visibility = View.VISIBLE
        nothingFoundText.visibility = View.VISIBLE
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

            val swipedPostData = postHistoryAdapter.getPostData(viewHolder.adapterPosition)

            swipedPostData?.let {
                compositeDisposable += viewModel.deleteDbPostData(swipedPostData)
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(onError = { e -> logD("Error deletingDbPostData in PostHistoryActivity: $e") })
            }
        }
    })


    class ConfirmationDialog : AppCompatDialogFragment() {

        var onYesSelect: ((Unit) -> Unit)? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =

            AlertDialog.Builder(activity).apply {

                setTitle("Are you sure?")

                setPositiveButton("Yes") { _, _ -> onYesSelect?.let { it(Unit) } }

                setNegativeButton("No") { _, _ -> }

            }.create()
    }

}
