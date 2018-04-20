package com.simplesln.data


import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

private var instance : MyDB? = null

@Database(entities = arrayOf(MediaFile::class), version = 1, exportSchema = false)
abstract class MyDB : RoomDatabase() {
    abstract fun library() : LibraryDAO
}

fun getInstance(context : Context) : MyDB?{
    if (instance == null) {
        instance = Room.databaseBuilder(context, MyDB::class.java!!, "my_db")
                .build()
    }
    return instance
}