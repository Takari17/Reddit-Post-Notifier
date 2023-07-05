package com.takari.redditpostnotifier.features.reddit.data

import com.takari.redditpostnotifier.features.reddit.newPost.models.NewPostListResponse
import com.takari.redditpostnotifier.features.reddit.subreddit.models.SubRedditDataResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface RedditApi {

    companion object {
        const val BASE_URL = "https://www.reddit.com"
    }

    @GET("https://www.reddit.com/r/{name}/new.json")
    suspend fun getNewPostList(@Path("name") name: String): NewPostListResponse

    @GET("https://www.reddit.com/r/{name}/about.json")
    suspend fun getSubRedditData(@Path("name") name: String): SubRedditDataResponse
}
