package com.example.androiddevhelper.data.remote.reddit

import com.example.androiddevhelper.data.remote.reddit.response.RedditPostResponse
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


const val BASE_URL = "https://www.reddit.com"
const val API = "https://www.reddit.com/r/androiddev/new/.json"

interface RedditApi {

    @GET(API)
    fun getAllPostData(): Single<RedditPostResponse>

    companion object{
        /*
        I don't think I need caching this way, we're gonna pull the data from fire base offline
         */
        operator fun invoke(): RedditApi {

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RedditApi::class.java)
        }
    }
}