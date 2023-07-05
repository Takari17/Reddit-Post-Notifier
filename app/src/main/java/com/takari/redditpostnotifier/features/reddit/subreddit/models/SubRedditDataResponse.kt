package com.takari.redditpostnotifier.features.reddit.subreddit.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.takari.redditpostnotifier.features.reddit.subreddit.models.SubRedditData.Companion.SUB_REDDIT_DATA_NAME
import kotlinx.android.parcel.Parcelize

data class SubRedditDataResponse(val data: SubRedditData)

@Entity(tableName = SUB_REDDIT_DATA_NAME)
@Parcelize
data class SubRedditData(
    @PrimaryKey
    @SerializedName("display_name_prefixed")
    val prefixedName: String,
    @SerializedName("display_name")
    val name: String,
    val icon_img: String,
    @SerializedName("public_description")
    val publicDescription: String
) : Parcelable {
    companion object {
        const val SUB_REDDIT_DATA_NAME = "sub_reddit_data"
    }
}



