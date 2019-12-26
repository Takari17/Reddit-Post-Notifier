package com.example.androiddevhelper.feature.postdata.data.remote

import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface RedditApi {

    @GET("https://www.reddit.com/r/{name}.json")
    fun validateSubReddit(@Path("name") name: String): Single<RedditResponse>

    /*
    Used an Observable instead of an Single because I need to be able to use flatMap for
    filtering stuff.
     */
    @GET("https://www.reddit.com/r/{name}/new.json")
    fun getAllPostData(@Path("name") name: String): Observable<RedditResponse>

    companion object{
        const val BASE_URL = "https://www.reddit.com"
    }
}
