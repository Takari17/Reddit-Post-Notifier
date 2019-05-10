package com.example.androiddevhelper.data.remote.reddit.response

import com.google.gson.annotations.SerializedName


data class RedditPostResponse(val `data`: Data)

data class Data(
    val children: List<NewRedditPost>,
    val dist: Int
)

/*
FireStore requiers no args constructors for deserialization
 */

data class NewRedditPost(val `data`: PostData){
    constructor(): this(PostData("", "", ""))
    }

/*
Todo: Create a new branch and try to directly return the Post Data instead of the NewRedditPost
 */
data class PostData(
    val author: String,
    val title: String,
    @SerializedName("permalink")
    val api: String //todo want to find the proper name for this
) {
    constructor() : this("", "", "")
}