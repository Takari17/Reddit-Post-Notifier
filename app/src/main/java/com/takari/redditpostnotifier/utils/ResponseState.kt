package com.takari.redditpostnotifier.utils

/**
 * TODO: add docs
 */
sealed class ResponseState<T> {
    data class Success<T>(val item: T) : ResponseState<T>()
    data class Error<T>(val e: Throwable) : ResponseState<T>()
}