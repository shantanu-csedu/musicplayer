package com.simplesln.data.dao

import android.arch.lifecycle.LiveData
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
    fun delete() : Int

    @Query("delete from media_library where id = :mediaId")
    fun delete(mediaId : Long)

    @Query("select * from media_library LIMIT :offset,:total")
    fun get(offset : Int, total : Int) : LiveData<List<MediaFile>>
}