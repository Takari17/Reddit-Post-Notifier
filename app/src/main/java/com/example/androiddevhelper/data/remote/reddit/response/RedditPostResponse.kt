package com.example.androiddevhelper.data.remote.reddit.response


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
    var documentId: String, // Auto generated ID fire base uses, need to keep track of this to prevent creating duplicates on each network call
    val author: String,
    val title: String
) {
    constructor() : this("", "", "")
}