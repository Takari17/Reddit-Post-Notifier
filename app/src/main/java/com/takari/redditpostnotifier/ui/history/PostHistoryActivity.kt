package com.takari.redditpostnotifier.ui.history

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.databinding.ActivityPostHistoryBinding
import com.takari.redditpostnotifier.misc.injectViewModel
import com.takari.redditpostnotifier.misc.openRedditPost
import com.takari.redditpostnotifier.misc.prependBaseUrlIfCrossPost


class PostHistoryActivity : AppCompatActivity() {

    private val viewModel: PostHistoryViewModel by injectViewModel { App.applicationComponent().postHistoryViewModel }
    private val confirmationDialog = ConfirmationDialog()
    private lateinit var newPostAdapter: NewPostAdapter
    private lateinit var binding: ActivityPostHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.navigationBarColor = Color.parseColor("#171A23")

        newPostAdapter = NewPostAdapter { clickedPostData ->

            val url = prependBaseUrlIfCrossPost(clickedPostData)
            this.openRedditPost(url)
            viewModel.deleteDbPostData(clickedPostData)
        }

        binding.postHistoryRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = newPostAdapter
            swipeHandler.attachToRecyclerView(this)
        }

        binding.deleteAllButton.setOnClickListener {
            if (!confirmationDialog.isAdded)
                confirmationDialog.show(supportFragmentManager, "ConfirmationDialog")
        }

        confirmationDialog.onYesSelect = { viewModel.deleteAllDbPostData() }

        viewModel.dbPostData.observe(this, Observer { postDataList ->
            if (postDataList.isNotEmpty()) {
                newPostAdapter.submitList(postDataList)
                showPostHistoryViews()
            } else showNothingFoundViews()
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    private fun showPostHistoryViews() {
        binding.postHistoryRecyclerView.visibility = View.VISIBLE

        binding.deleteAllButton.visibility = View.VISIBLE
        binding.deleteAllButton.isEnabled = true

        binding.nothingFoundIcon.visibility = View.INVISIBLE
        binding.nothingFoundText.visibility = View.INVISIBLE
    }

    private fun showNothingFoundViews() {
        binding.postHistoryRecyclerView.visibility = View.INVISIBLE

        binding.deleteAllButton.visibility = View.INVISIBLE
        binding.deleteAllButton.isEnabled = false

        binding.nothingFoundIcon.visibility = View.VISIBLE
        binding.nothingFoundText.visibility = View.VISIBLE
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

            if (swipedPostData != null) viewModel.deleteDbPostData(swipedPostData)
        }
    })


    class ConfirmationDialog : AppCompatDialogFragment() {

        var onYesSelect: ((Unit) -> Unit) = {}

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =

            AlertDialog.Builder(activity).apply {

                setTitle("Are you sure?")

                setPositiveButton("Yes") { _, _ -> onYesSelect(Unit) }

                setNegativeButton("No") { _, _ -> }

            }.create()
    }

}
