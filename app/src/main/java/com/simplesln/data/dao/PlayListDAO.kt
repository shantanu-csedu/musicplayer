package com.simplesln.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.simplesln.data.entities.PlayList

@Dao
interface PlayListDAO {
    @Query("select * from media_playlist")
    fun getPlaylist() : LiveData<List<PlayList>>
}