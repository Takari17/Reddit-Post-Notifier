package com.takari.redditpostnotifier.features.reddit.newPostHistory

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.databinding.ActivityPostHistoryBinding
import com.takari.redditpostnotifier.utils.openRedditPost
import com.takari.redditpostnotifier.utils.prependBaseUrlIfCrossPost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostHistoryFragment : Fragment() {

    companion object {
        const val TAG = "Post History"
    }

    private val viewModel: PostHistoryViewModel by activityViewModels()
    private val confirmationDialog = ConfirmationDialog()
    private lateinit var newPostAdapter: NewPostAdapter
    private val binding by lazy { ActivityPostHistoryBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Adds the back button to the action bar and changes it's name

        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar

        supportActionBar?.title = resources.getString(R.string.post_history)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newPostAdapter = NewPostAdapter { clickedPostData ->

            val url = prependBaseUrlIfCrossPost(clickedPostData)
            requireContext().openRedditPost(url)
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
                confirmationDialog.show(
                    requireActivity().supportFragmentManager,
                    "ConfirmationDialog"
                )
        }

        confirmationDialog.onYesSelect = { viewModel.deleteAllDbPostData() }

        viewModel.dbPostData.observe(viewLifecycleOwner, Observer { postDataList ->
            if (postDataList.isNotEmpty()) {
                newPostAdapter.submitList(postDataList)
                showPostHistoryViews()
            } else showNothingFoundViews()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Reverts action bar
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        supportActionBar?.title = resources.getString(R.string.app_name)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
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