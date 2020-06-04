package com.takari.redditpostnotifier.misc

import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Start by subscribing and stop by unsubscribing.
 */
class RepeatingCountDownTimer(private val millis: Long) {

    // atomic's are used for thread safety
    private val elapsedTime = AtomicLong().apply { addAndGet(millis) }
    private val stopTimer = AtomicBoolean().apply { set(false) }

    val get: Observable<String> = Observable.interval(1, TimeUnit.SECONDS)
        .takeWhile { !stopTimer.get() }
        .map {
            if (elapsedTime.get() == 0L)
                elapsedTime.set(millis) // adds initial value back to the elapse time

            it
        }
        .map {
            elapsedTime.addAndGet(-1000)
            elapsedTime.get().toSecondsFormat()
        }

    private fun Long.toSecondsFormat(): String {
        val seconds = (this / 1000).toInt()
        return seconds.toString()
    }
}