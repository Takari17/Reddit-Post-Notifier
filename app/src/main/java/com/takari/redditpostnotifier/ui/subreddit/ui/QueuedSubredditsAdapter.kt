package com.takari.redditpostnotifier.ui.subreddit.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.data.subreddit.SubRedditData
import kotlinx.android.synthetic.main.queued_subreddits_layout.view.*


class QueuedSubredditsAdapter(
    private val onSwipe: (SubRedditData) -> Unit
) : ListAdapter<SubRedditData, QueuedSubredditsAdapter.ViewHolder>(
    QueuedSubRedditDiffCallback()
) {

    private val swipeHandler = ItemTouchHelper(object :
        ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT //Only can swipe right or left
        ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false //Don't need this callback

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            onSwipe.invoke(getItem(viewHolder.adapterPosition))
        }
    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.queued_subreddits_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {

            if (getItem(position).icon_img.isEmpty()) iconImageView.load(R.drawable.default_sub_icon)
            else iconImageView.load(getItem(position).icon_img)

            subRedditTextName.text = getItem(position).prefixedName
            descriptionTextView.text = getItem(position).publicDescription
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.iconImageView
        val subRedditTextName: TextView = itemView.subRedditTextName
        val descriptionTextView: TextView = itemView.descriptionTextView
    }

    fun attachOnSwipe(recyclerView: RecyclerView) {
        swipeHandler.attachToRecyclerView(recyclerView)
    }

    private class QueuedSubRedditDiffCallback : DiffUtil.ItemCallback<SubRedditData>() {
        override fun areItemsTheSame(oldItem: SubRedditData, newItem: SubRedditData): Boolean {
            return oldItem.prefixedName == newItem.prefixedName
        }

        override fun areContentsTheSame(oldItem: SubRedditData, newItem: SubRedditData): Boolean {
            return oldItem.prefixedName == newItem.prefixedName
        }
    }
}
