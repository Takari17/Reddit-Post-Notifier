package com.example.androiddevhelper.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost

/*
Defines the columns for your table, this table only has 2 columns, 1 for an Id and another for the user inputted data
 */
@Entity(tableName = "data_table")
data class DbData(
    @PrimaryKey(autoGenerate = true)
    val id: Int
//    val previousRedditPost: List<NewRedditPost>
)