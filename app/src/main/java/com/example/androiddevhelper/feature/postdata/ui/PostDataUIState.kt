package com.example.androiddevhelper.feature.postdata.ui

import com.example.androiddevhelper.feature.postdata.data.local.PostData


data class PostDataUIState(
    val subRedditStatus: PostDataFragment.SubRedditStatus,
    val isLoading: Boolean,
    val postDataList: List<PostData>,
    val subReddit: String
)
