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

package com.simplesln.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.simplesln.data.entities.MediaFile

@Dao
interface LibraryDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(mediaFiles : MediaFile)

    @Update
    fun update(mediaFiles : List<MediaFile>)

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