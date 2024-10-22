package com.takari.redditpostnotifier.features.reddit.newPost.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.takari.redditpostnotifier.databinding.FragmentObservingBinding
import com.takari.redditpostnotifier.features.MainActivity
import com.takari.redditpostnotifier.features.reddit.SharedViewModel
import com.takari.redditpostnotifier.features.reddit.newPost.service.NewPostService
import com.takari.redditpostnotifier.features.reddit.newPostHistory.NewPostAdapter
import com.takari.redditpostnotifier.features.reddit.subreddit.models.SubRedditData
import com.takari.redditpostnotifier.utils.logD
import com.takari.redditpostnotifier.utils.openRedditPost
import com.takari.redditpostnotifier.utils.prependBaseUrlIfCrossPost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        const val TAG = "New Post"
    }

    private val viewModel: SharedViewModel by activityViewModels()
    private val subIconAdapter = ChosenSubRedditAdapter()
    private val serviceIntent by lazy { Intent(context, NewPostService::class.java) }
    private lateinit var newPostAdapter: NewPostAdapter
    private val binding by lazy { FragmentObservingBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.stopObservingButton.setOnClickListener {
            val mainActivity: MainActivity = requireActivity() as MainActivity
            mainActivity.switchToSubRedditFragment()

            requireContext().stopService(serviceIntent)
        }

        newPostAdapter = NewPostAdapter { clickedPostData ->

            val url = prependBaseUrlIfCrossPost(clickedPostData)
            requireContext().openRedditPost(url)
            viewModel.deleteDbPostData(clickedPostData)
        }

        binding.subRedditIconRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = subIconAdapter
        }

        binding.postHistoryRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = newPostAdapter
            swipeHandler.attachToRecyclerView(this)
        }

        viewModel.dbSubRedditData.observe(viewLifecycleOwner, Observer { subRedditData ->
            subIconAdapter.submitList(subRedditData)
        })

        viewModel.dbPostData.observe(viewLifecycleOwner, Observer { postDataList ->
            newPostAdapter.submitList(postDataList)
        })
    }

    override fun onStart() {
        super.onStart()

        if (!NewPostService.isRunning()) {
            startService()
            bindToService()
        } else bindToService()
    }

    override fun onStop() {
        super.onStop()
        unbindFromService()
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
            swipedPostData?.let { viewModel.deleteDbPostData(swipedPostData) }
        }
    })

    private fun startService() {
        serviceIntent.putParcelableArrayListExtra(
            SubRedditData.SUB_REDDIT_DATA_NAME,
            //cant be null at this point
            ArrayList(viewModel.subRedditDataList!!)
        )
        requireContext().startService(serviceIntent)
    }

    private fun bindToService() {
        requireContext().bindService(serviceIntent, serviceConnection, 0)
    }

    private fun unbindFromService() {
        requireContext().unbindService(serviceConnection)
    }

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val postDataService = (service as NewPostService.LocalBinder).getService()

            //Service is already in the main thread
            postDataService.observeCurrentTime().observe(viewLifecycleOwner) { timeInSeconds ->
                binding.timerTextView.text = "Connecting In: $timeInSeconds secs"
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            logD("UNBINDED FROM SERVICE ---------------")
            val mainActivity: MainActivity = requireActivity() as MainActivity
            mainActivity.switchToSubRedditFragment()
        }
    }
}