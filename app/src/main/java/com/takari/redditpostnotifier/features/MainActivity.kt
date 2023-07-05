package com.takari.redditpostnotifier.features

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.databinding.PostDataActivityBinding
import com.takari.redditpostnotifier.features.reddit.newPost.service.NewPostService
import com.takari.redditpostnotifier.features.reddit.newPost.ui.NewPostFragment
import com.takari.redditpostnotifier.features.reddit.newPostHistory.PostHistoryFragment
import com.takari.redditpostnotifier.features.reddit.subreddit.ui.SubRedditFragment
import com.takari.redditpostnotifier.features.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val subRedditFragment = SubRedditFragment()
    private val newPostFragment = NewPostFragment()
    private val settingsFragment = SettingsFragment()
    private val postHistoryFragment = PostHistoryFragment()
    private lateinit var binding: PostDataActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PostDataActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.navigationBarColor = Color.parseColor("#171A23")

        if (savedInstanceState == null) {
            val fragment: Fragment
            val tag: String

            if (NewPostService.isRunning()) {
                fragment = newPostFragment
                tag = NewPostFragment.TAG
            } else {
                fragment = subRedditFragment
                tag = SubRedditFragment.TAG
            }

            supportFragmentManager.commit {
                add(binding.container.id, fragment, tag)
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
                if (!postHistoryFragment.isAdded) supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.enter_from_right, R.anim.exit_to_left,
                        R.anim.enter_from_left, R.anim.exit_to_right,
                    )

                    addToBackStack(PostHistoryFragment.TAG)
                    replace(binding.container.id, postHistoryFragment, PostHistoryFragment.TAG)
                }
            }

            R.id.settingsOption -> {
                if (!settingsFragment.isAdded) supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.enter_from_right, R.anim.exit_to_left,
                        R.anim.enter_from_left, R.anim.exit_to_right,
                    )

                    addToBackStack(SettingsFragment.TAG)
                    replace(binding.container.id, settingsFragment, SettingsFragment.TAG)
                }
            }

            else -> supportFragmentManager.popBackStack()
        }

        return super.onOptionsItemSelected(item)
    }

    fun switchToSubRedditFragment() = supportFragmentManager.commit {
        setCustomAnimations(R.anim.slide_up_in, R.anim.slide_up_out)
        replace(binding.container.id, subRedditFragment, SubRedditFragment.TAG)
    }

    fun switchToNewPostFragment() = supportFragmentManager.commit {
        setCustomAnimations(R.anim.slide_down_in, R.anim.slide_down_out)
        replace(binding.container.id, newPostFragment, NewPostFragment.TAG)
    }
}
