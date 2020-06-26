package com.takari.redditpostnotifier.misc

sealed class ResponseState<T> {
    data class Success<T>(val item: T) : ResponseState<T>()
    data class Error<T>(val e: Throwable) : ResponseState<T>()
}