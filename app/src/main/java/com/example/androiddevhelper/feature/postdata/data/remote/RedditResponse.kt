package com.example.androiddevhelper.feature.postdata.data.remote

import com.example.androiddevhelper.feature.postdata.data.local.PostData
import com.google.gson.annotations.Expose

data class RedditResponse(
    @Expose
    val `data`: Data
)

data class Data(
    @Expose
    val children: List<Post>,
    @Expose
    val dist: Int
)

data class Post(
    @Expose
    val data: PostData
)

