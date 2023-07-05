package com.takari.redditpostnotifier.features.reddit.subreddit.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.databinding.PostDataActivityBinding
import com.takari.redditpostnotifier.features.reddit.SharedViewModel
import com.takari.redditpostnotifier.utils.injectViewModel
import com.takari.redditpostnotifier.features.reddit.newPostHistory.PostHistoryActivity
import com.takari.redditpostnotifier.features.reddit.newPost.service.NewPostService
import com.takari.redditpostnotifier.features.reddit.newPost.ui.NewPostFragment
import com.takari.redditpostnotifier.features.settings.SettingsActivity

/**
 * This package strucutre is terrible. We'll organize it by feature.
 *
 * Features:
 *  settings
 *  common
 *  postHistory
 *  newPost (monitoring)
 *  subReddit (main screen)
 *
 *
 * Reddit
 *  SubReddits
 *      UI
 *  NewPost
 *      UI
 *  Data
 */

class MainActivity : AppCompatActivity() {

    companion object {
        const val SUB_REDDIT_FRAGMENT = "post data fragment"
        const val OBSERVING_FRAGMENT = "observing fragment"
    }

    private val viewModel by injectViewModel { App.applicationComponent().sharedViewModel }
    private lateinit var subRedditFragment: Fragment
    private lateinit var newPostFragment: Fragment
    private lateinit var binding: PostDataActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PostDataActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.navigationBarColor = Color.parseColor("#171A23")

        //checks if fragments already exist to avoid creating duplicates
        subRedditFragment = supportFragmentManager.findFragmentByTag(SUB_REDDIT_FRAGMENT)
            ?: SubRedditFragment()

        newPostFragment = supportFragmentManager.findFragmentByTag(OBSERVING_FRAGMENT)
            ?: NewPostFragment()

        if (savedInstanceState == null) {

            if (NewPostService.isRunning())
                addInitialFragment(newPostFragment, OBSERVING_FRAGMENT)
            else
                addInitialFragment(subRedditFragment, SUB_REDDIT_FRAGMENT)
        }

        viewModel.switchContainers = { fragmentName ->
            when (fragmentName) {
                SharedViewModel.FragmentName.SubRedditFragment -> {
                    if (!subRedditFragment.isAdded) switchToSubRedditFragment()
                }

                SharedViewModel.FragmentName.NewPostFragment -> {
                    if (!newPostFragment.isAdded) switchToNewPostFragment()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.example_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.postHistoryItem -> {
                val postHistoryIntent = Intent(this, PostHistoryActivity::class.java)
                startActivity(postHistoryIntent)
            }

            R.id.settingsOption -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun addInitialFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
            .beginTransaction()
            .add(binding.container.id, fragment, tag)
            .commit()
    }

    private fun switchToSubRedditFragment() {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_up_out)
            .replace(binding.container.id, subRedditFragment, SUB_REDDIT_FRAGMENT)
            .commit()
    }

    private fun switchToNewPostFragment() {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_down_in, R.anim.slide_down_out)
            .replace(binding.container.id, newPostFragment, OBSERVING_FRAGMENT)
            .commit()
    }
}
