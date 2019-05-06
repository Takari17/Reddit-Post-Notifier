package com.example.androiddevhelper.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DbData::class], version = 1)

abstract class DataBase: RoomDatabase() {
    abstract fun dao(): Dao

    // Thread Safe
    companion object{
        @Volatile private var instance: DataBase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                DataBase::class.java, "sql_data_bale")
                .build()
    }
}