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
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.PlayList
import com.simplesln.data.entities.PlayListData

@Dao
interface PlayListDAO {
    @Query("select * from media_playlist")
    fun getPlaylist() : LiveData<List<PlayList>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(playlist: PlayList) : Long

    @Query("select media_library.* from media_playlist_data left join media_library on media_playlist_data.media_file_id = media_library.id left join media_playlist on media_playlist.id = media_playlist_data.media_playlist_id where media_playlist.name = :name")
    fun getMediaFiles(name : String) : LiveData<List<MediaFile>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPlayListData(playlistData : PlayListData) : Long

    @Query("select id from media_playlist where name=:name")
    fun getPlaylistId(name : String) : Long

    @Query("delete from media_playlist where name =:name")
    fun deletePlaylist(name : String) : Int

    @Query("delete from media_playlist_data where media_file_id =:mediaId and media_playlist_id = (select id from media_playlist where name=:playlistName limit 1)")
    fun deleteMusic(mediaId : Long, playlistName : String)
}