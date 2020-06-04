package com.takari.redditpostnotifier.ui.post.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.data.subreddit.SubRedditData
import kotlinx.android.synthetic.main.icon_adapter_layout.view.*


class ChosenSubRedditAdapter : ListAdapter<SubRedditData, ChosenSubRedditAdapter.ViewHolder>(
    SubRedditDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.icon_adapter_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (getItem(position).icon_img.isEmpty())
            holder.icon.load(R.drawable.default_sub_icon)
        else
            holder.icon.load(getItem(position).icon_img)

        holder.subRedditName.text = getItem(position).prefixedName
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.icon
        val subRedditName: TextView = itemView.subRedditName
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
