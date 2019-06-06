package com.example.androiddevhelper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import javax.inject.Singleton

@Singleton
@Database(entities = [PostData::class], version = 2)
abstract class DataBase : RoomDatabase() {
    abstract fun dao(): Dao
}