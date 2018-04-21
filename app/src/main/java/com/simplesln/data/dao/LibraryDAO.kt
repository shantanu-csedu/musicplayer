package com.simplesln.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.simplesln.data.entities.MediaFile

@Dao
interface LibraryDAO {
    @Insert
    fun insert(mediaFiles : List<MediaFile>)

    @Query("delete from media_library")
    fun delete()

    @Query("delete from media_library where id = :mediaId")
    fun delete(mediaId : Long)
}