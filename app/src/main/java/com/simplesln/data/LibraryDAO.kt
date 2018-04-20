package com.simplesln.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert

@Dao
interface LibraryDAO {
    @Insert
    fun insert(mediaFiles : List<MediaFile>)
}