package com.example.androiddevhelper.data.remote.reddit.response


data class Image(
    val id: String,
    val resolutions: List<Resolution>,
    val source: Source,
    val variants: Variants
)