package com.example.androiddevhelper.feature.postdata.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androiddevhelper.feature.postdata.data.local.PostData.Companion.REDDIT_POST_DATA
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


@Entity(tableName = REDDIT_POST_DATA)
data class PostData(
    //Retrofit will ignore this since it's missing the Expose annotation (which is good)
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @Expose
    val author: String,
    @Expose
    val title: String,
    @SerializedName("permalink")
    @Expose
    val sourceUrl: String
){
    companion object{
        const val REDDIT_POST_DATA = "reddit_post_data_table"
    }
}
