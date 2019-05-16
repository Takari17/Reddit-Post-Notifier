package com.example.androiddevhelper.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androiddevhelper.utils.ENTITY_TABLE_NAME
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


@Entity(tableName = ENTITY_TABLE_NAME)
data class PostData(
    //Retrofit will ignore the primary key id because it's missing the Expose annotation
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @Expose
    val author: String,
    @Expose
    val title: String,
    @SerializedName("permalink")
    @Expose
    val api: String,
    @SerializedName("selftext")
    @Expose
    val description: String
) {
    constructor() : this(0, "", "", "", "")
}