package com.simplesln.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.PlayList

@Dao
interface PlayListDAO {
    @Query("select * from media_playlist")
    fun getPlaylist() : LiveData<List<PlayList>>

    @Query("select media_library.* from media_playlist_data left join media_library on media_playlist_data.media_file_id = media_library.id left join media_playlist on media_playlist.id = media_playlist_data.media_playlist_id where media_playlist.name = :name")
    fun getMediaFiles(name : String) : LiveData<List<MediaFile>>
}