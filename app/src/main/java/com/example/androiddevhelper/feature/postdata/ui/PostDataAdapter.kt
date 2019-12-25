package com.example.androiddevhelper.feature.postdata.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androiddevhelper.R
import com.example.androiddevhelper.feature.postdata.data.local.PostData
import com.example.androiddevhelper.feature.postdata.ui.PostDataAdapter.MainViewHolder
import com.example.androiddevhelper.openRedditPostWithToast
import kotlinx.android.synthetic.main.new_reddit_post_layout.view.*
import javax.inject.Inject


class PostDataAdapter @Inject constructor(
    private val context: Context,
    private val itemClick: (Int) -> Unit
) : ListAdapter<PostData, MainViewHolder>(CustomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.new_reddit_post_layout, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.apply {
            title.text = getItem(position).title
            author.text = "${getItem(position).author} posted:"
        }
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.titleTextView
        val author: TextView = itemView.authorTextView

        init {
            itemView.setOnClickListener {
                val api = getItem(adapterPosition).sourceUrl
                openRedditPostWithToast(
                    context,
                    api
                )
                itemClick(adapterPosition)
            }
        }
    }

    fun updateList(newPostList: List<PostData>) {
        submitList(newPostList)
    }

    fun fetchItem(position: Int): PostData? = getItem(position)


    private class CustomDiffCallback : DiffUtil.ItemCallback<PostData>() {
        override fun areItemsTheSame(
            oldItem: PostData,
            newItem: PostData
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PostData,
            newItem: PostData
        ): Boolean {
            return oldItem == newItem
        }
    }
}
