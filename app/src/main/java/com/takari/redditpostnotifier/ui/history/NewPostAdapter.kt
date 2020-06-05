package com.takari.redditpostnotifier.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.data.post.Post
import com.takari.redditpostnotifier.data.post.PostData
import com.takari.redditpostnotifier.ui.history.NewPostAdapter.MainViewHolder
import io.reactivex.Single
import kotlinx.android.synthetic.main.post_data_layout.view.*
import javax.inject.Inject


class NewPostAdapter @Inject constructor(private val onClick: (PostData) -> Unit) :
    ListAdapter<PostData, MainViewHolder>(PostDataDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.post_data_layout, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.apply {
            title.text = getItem(position).title
            postInfo.text = "u/${getItem(position).author} from r/${getItem(position).subReddit} posted:"
        }
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.postTitleTextView
        val postInfo: TextView = itemView.postInfoTextView

        init {
            itemView.setOnClickListener {
                val clickedPostData = getItem(adapterPosition)
                onClick(clickedPostData)
            }
        }
    }

    fun getPostData(adapterPosition: Int): PostData? = getItem(adapterPosition)

    private class PostDataDiffCallback : DiffUtil.ItemCallback<PostData>() {
        override fun areItemsTheSame(oldItem: PostData, newItem: PostData): Boolean {
            return oldItem.sourceUrl == newItem.sourceUrl
        }

        override fun areContentsTheSame(oldItem: PostData, newItem: PostData): Boolean {
            return oldItem.sourceUrl == newItem.sourceUrl
        }
    }
}