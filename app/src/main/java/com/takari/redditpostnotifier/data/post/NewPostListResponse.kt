package com.takari.redditpostnotifier.data.post

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


data class NewPostListResponse(val `data`: Data)

data class Data(val children: List<Post>)

data class Post(val data: PostData)

@Entity(tableName = "post_data_table")
data class PostData(
    @PrimaryKey
    @SerializedName("url")
    val sourceUrl: String,
    @SerializedName("subreddit")
    val subReddit: String,
    val author: String,
    val title: String
)
