package com.takari.redditpostnotifier.features.reddit.newPost.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.features.reddit.subreddit.models.SubRedditData
import com.takari.redditpostnotifier.databinding.IconAdapterLayoutBinding


class ChosenSubRedditAdapter : ListAdapter<SubRedditData, ChosenSubRedditAdapter.ViewHolder>(
    SubRedditDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = IconAdapterLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (getItem(position).icon_img.isEmpty())
            holder.icon.load(R.drawable.default_sub_icon)
        else
            holder.icon.load(getItem(position).icon_img)

        holder.subRedditName.text = getItem(position).prefixedName
    }

    inner class ViewHolder(binding: IconAdapterLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val icon: ImageView = binding.icon
        val subRedditName: TextView = binding.subRedditName
    }

    private class SubRedditDiffCallback : DiffUtil.ItemCallback<SubRedditData>() {
        override fun areItemsTheSame(oldItem: SubRedditData, newItem: SubRedditData): Boolean {
            return oldItem.prefixedName == newItem.prefixedName
        }

        override fun areContentsTheSame(oldItem: SubRedditData, newItem: SubRedditData): Boolean {
            return oldItem.prefixedName == newItem.prefixedName
        }
    }
}
