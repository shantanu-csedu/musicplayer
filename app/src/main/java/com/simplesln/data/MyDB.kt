/*
 * Copyright (c) 2018.  shantanu saha <shantanu.csedu@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

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