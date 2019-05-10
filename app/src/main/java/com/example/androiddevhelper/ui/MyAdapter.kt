package com.example.androiddevhelper.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androiddevhelper.R
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost
import kotlinx.android.synthetic.main.new_reddit_post_layout.view.*

/*
todo want it so our recycler view is restored on start

todo I want our recycler view to always be visisble, even if there aren't any values
 */
class MyAdapter(private var newRedditPost: List<NewRedditPost>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.new_reddit_post_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = newRedditPost.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = newRedditPost[position].data.title
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.titleTextView
    }

    fun updateNewRedditPost(recentPost: List<NewRedditPost>){
        newRedditPost = recentPost
    }
}