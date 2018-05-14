package com.simplesln.data


import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.simplesln.data.dao.LibraryDAO
import com.simplesln.data.dao.MediaQueueDAO
import com.simplesln.data.dao.NowPlayDAO
import com.simplesln.data.dao.PlayListDAO
import com.simplesln.data.entities.*
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.PlayList

private var instance : MyDB? = null

@Database(entities = arrayOf(MediaFile::class,MediaQueue::class,PlayList::class,PlayListData::class,NowPlay::class), version = 1, exportSchema = false)
abstract class MyDB : RoomDatabase() {
    abstract fun library() : LibraryDAO
    abstract fun queue() : MediaQueueDAO
    abstract fun playlist() : PlayListDAO
    abstract fun nowPlay() : NowPlayDAO
}

fun getMyDBInstance(context : Context) : MyDB?{
    if (instance == null) {
        instance = Room.databaseBuilder(context, MyDB::class.java, "my_db")
                .build()
    }
    return instance
}