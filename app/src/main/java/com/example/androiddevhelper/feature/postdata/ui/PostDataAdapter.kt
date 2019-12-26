package com.example.androiddevhelper.feature.postdata.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androiddevhelper.R
import com.example.androiddevhelper.feature.postdata.data.local.PostData
import com.example.androiddevhelper.feature.postdata.ui.PostDataAdapter.MainViewHolder
import kotlinx.android.synthetic.main.post_data_layout.view.*
import javax.inject.Inject


class PostDataAdapter @Inject constructor(
    private val itemClick: (PostData) -> Unit,
    private val onSwipe: (PostData) -> Unit
) : ListAdapter<PostData, MainViewHolder>(CustomDiffCallback()) {

    private val swipeHandler = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_data_layout, parent, false)
        return MainViewHolder(view)
    }


    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.apply {
            title.text = getItem(position).title
            author.text = "u/${getItem(position).author}"
        }
    }


    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.titleTextView
        val author: TextView = itemView.authorTextView

        init {
            itemView.setOnClickListener {
                itemClick(getItem(adapterPosition))
            }
        }
    }

    fun update(newPostList: List<PostData>) {
        submitList(newPostList)
    }


    fun attachOnSwipe(recyclerView: RecyclerView) {
        swipeHandler.attachToRecyclerView(recyclerView)
    }


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
