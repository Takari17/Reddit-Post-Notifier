package com.takari.redditpostnotifier.misc

sealed class ResponseState<T> {
    data class Success<T>(val item: T? = null) : ResponseState<T>()
    data class Error<T>(val eMsg: String) : ResponseState<T>()
}