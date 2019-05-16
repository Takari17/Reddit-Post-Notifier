package com.example.androiddevhelper.data.remote.reddit

import com.example.androiddevhelper.utils.API
import io.reactivex.Single
import retrofit2.http.GET

interface RedditApi {

    @GET(API)
    fun getAllPostData(): Single<RedditPostResponse>
}