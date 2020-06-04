package com.takari.redditpostnotifier.data.misc

import com.takari.redditpostnotifier.data.post.NewPostListResponse
import com.takari.redditpostnotifier.data.subreddit.SubRedditDataResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface RedditApi {

    companion object{
        const val BASE_URL = "https://www.reddit.com"
    }

    @GET("https://www.reddit.com/r/{name}/new.json")
    fun getNewPostList(@Path("name") name: String): Single<NewPostListResponse>

    @GET("https://www.reddit.com/r/{name}/about.json")
    fun getSubRedditData(@Path("name") name: String): Single<SubRedditDataResponse>
}
