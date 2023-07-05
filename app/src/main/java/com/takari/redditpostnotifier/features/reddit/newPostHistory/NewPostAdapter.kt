package com.takari.redditpostnotifier.features.reddit.newPostHistory

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.takari.redditpostnotifier.features.reddit.newPost.models.PostData
import com.takari.redditpostnotifier.databinding.PostDataLayoutBinding
import com.takari.redditpostnotifier.features.reddit.newPostHistory.NewPostAdapter.NewPostViewHolder
import javax.inject.Inject


class NewPostAdapter @Inject constructor(private val onClick: (PostData) -> Unit) :
    ListAdapter<PostData, NewPostViewHolder>(PostDataDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewPostViewHolder {
        val binding = PostDataLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return NewPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewPostViewHolder, position: Int) {
        holder.apply {
            title.text = getItem(position).title
            postInfo.text =
                "u/${getItem(position).author} from r/${getItem(position).subReddit} posted:" //todo
        }
    }


    fun getPostData(adapterPosition: Int): PostData? = getItem(adapterPosition)

    inner class NewPostViewHolder(binding: PostDataLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val title: TextView = binding.postTitleTextView
        val postInfo: TextView = binding.postInfoTextView

        init {
            binding.root.setOnClickListener {
                val clickedPostData = getItem(adapterPosition)
                onClick(clickedPostData)
            }
        }
    }

    private class PostDataDiffCallback : DiffUtil.ItemCallback<PostData>() {
        override fun areItemsTheSame(oldItem: PostData, newItem: PostData): Boolean {
            return oldItem.sourceUrl == newItem.sourceUrl
        }

        override fun areContentsTheSame(oldItem: PostData, newItem: PostData): Boolean {
            return oldItem.sourceUrl == newItem.sourceUrl
        }
    }
}