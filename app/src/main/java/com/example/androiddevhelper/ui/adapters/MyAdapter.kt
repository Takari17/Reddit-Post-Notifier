package com.example.androiddevhelper.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androiddevhelper.R
import com.example.androiddevhelper.data.local.PostData
import com.example.androiddevhelper.ui.adapters.MyAdapter.MainViewHolder
import com.example.androiddevhelper.utils.getUserString
import com.example.androiddevhelper.utils.openRedditPostWithToast
import kotlinx.android.synthetic.main.new_reddit_post_layout.view.*
import javax.inject.Inject

/*
 * Observes the local database, service sends data to local data base,
 * code is decoupled, everyone's happy ;D
 */

/*
We can show the author, title

u/userName Posted: Title, days ago posted
 */
class MyAdapter @Inject constructor(
    private val context: Context,
    private val itemClick: (Int) ->  Unit
) : ListAdapter<PostData, MainViewHolder>(CustomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.new_reddit_post_layout, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.apply {
            title.text = getItem(position).title
            author.text = getUserString(context, R.string.user_posted, getItem(position).author)
        }
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.titleTextView
        val author: TextView = itemView.authorTextView

        init {
            itemView.setOnClickListener {
                val api = getItem(adapterPosition).api
                openRedditPostWithToast(context, api)
                itemClick(adapterPosition)

            }
        }
    }

    fun updateList(newPostData: List<PostData>) = submitList(newPostData)

    fun fetchItem(position: Int): PostData? = getItem(position)
}

class CustomDiffCallback : DiffUtil.ItemCallback<PostData>() {
    override fun areItemsTheSame(oldItem: PostData, newItem: PostData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PostData, newItem: PostData): Boolean {
        return oldItem == newItem
    }
}