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

    @Query("select * from media_library")
    fun get() : LiveData<List<MediaFile>>

    @Query("select folder from media_library group by folder")
    fun getAlbum() : LiveData<List<String>>

    @Query("select * from media_library where folder=:name")
    fun getMediaListByAlbum(name : String) : LiveData<List<MediaFile>>

    @Query("select artist from media_library group by artist")
    fun getArtist() : LiveData<List<String>>

    @Query("select genre from media_library group by genre")
    fun getGenre(): LiveData<List<String>>

    @Query("select * from media_library where genre=:name")
    fun getMediaListByGenre(name: String): LiveData<List<MediaFile>>

    @Query("select * from media_library where artist=:name")
    fun getMediaListByArtist(name: String): LiveData<List<MediaFile>>
}