package com.example.androiddevhelper.data.remote.reddit

import com.example.androiddevhelper.utils.API
import io.reactivex.Observable
import retrofit2.http.GET

interface RedditApi {

    /*
    Returns an Observable instead of a Single because I need to iterate though the list that's returned which
    you cannot do with a Single.
     */
    @GET(API)
    fun getAllPostData(): Observable<RedditResponse>
}