package com.example.androiddevhelper.feature

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.androiddevhelper.R
import com.example.androiddevhelper.feature.postdata.ui.PostDataFragment

class MainActivity : AppCompatActivity() {

    //todo look into why zwi said this was bad, then take notes.
    private val postListFragment = PostDataFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        replaceContainer(postListFragment)
    }

    private fun replaceContainer(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }


    companion object {
        fun createIntent(context: Context) =
            Intent(context, MainActivity::class.java)
    }
}
