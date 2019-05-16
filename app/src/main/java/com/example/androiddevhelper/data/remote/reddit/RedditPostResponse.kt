package com.example.androiddevhelper.data.remote.reddit

import com.example.androiddevhelper.data.local.PostData
import com.google.gson.annotations.Expose


data class RedditPostResponse(
    @Expose
    val `data`: Data
)

data class Data(
    @Expose
    val children: List<NewRedditPost>,
    @Expose
    val dist: Int
)

data class NewRedditPost(
    @Expose
    val `data`: PostData){
    constructor(): this(PostData(0, "", "", "", ""))
}