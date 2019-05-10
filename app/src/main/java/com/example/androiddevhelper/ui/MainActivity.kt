package com.example.androiddevhelper.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androiddevhelper.R
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost
import com.example.androiddevhelper.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: MyAdapter
    private var recyclerViewCreated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel.newRedditPostList.observe(this, Observer { newRedditPostList ->
            if (!recyclerViewCreated) initRecyclerView(newRedditPostList) else updateNewRedditPost(newRedditPostList)
        })

        startServiceButton.setOnClickListener { viewModel.startService() }

        serviceResetButton.setOnClickListener { viewModel.resetService() }
    }


    private fun initRecyclerView(newRedditPost: List<NewRedditPost>){
        // todo look into "also" for whatever reason it wasn't running
        val myAdapter = MyAdapter(newRedditPost)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = myAdapter
        }
        recyclerViewCreated = true
        adapter = myAdapter
    }

    private fun updateNewRedditPost(recentPost: List<NewRedditPost>){
        adapter.updateNewRedditPost(recentPost)
        adapter.notifyDataSetChanged() //todo I belive we can be more specific about where we insert the values, doing so makes for performance I think
    }


    override fun onStart() {
        super.onStart()
        viewModel.bindToService()
    }

    override fun onStop() {
        super.onStop()
        viewModel.unbindFromService()
    }
}