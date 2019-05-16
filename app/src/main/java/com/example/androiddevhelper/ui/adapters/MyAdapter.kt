package com.example.androiddevhelper.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androiddevhelper.R
import com.example.androiddevhelper.data.local.PostData
import com.example.androiddevhelper.utils.openRedditPost
import kotlinx.android.synthetic.main.new_reddit_post_layout.view.*
import javax.inject.Inject

/**
 * Observes the local database, service sends data to data base,
 * code is decoupled, everyone's happy
 */

class MyAdapter @Inject constructor(
    private val context: Context
) : RecyclerView.Adapter<MyAdapter.MainViewHolder>() {

    var newPostDataList = emptyList<PostData>().toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.new_reddit_post_layout, parent, false)
        return MainViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (newPostDataList.isEmpty())
            1
        else
            newPostDataList.size
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        if (newPostDataList.isEmpty()) {
            holder.title.text = "No New Post"
        } else {
            holder.title.text = newPostDataList[position].title
        }
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.titleTextView

        init {
            itemView.setOnClickListener {
                val api = newPostDataList[adapterPosition].api
                openRedditPost(context, api)
            }
        }
    }

    fun updateNewRedditPost(newPostData: List<PostData>) {
        newPostDataList = newPostData.toMutableList()
    }
}