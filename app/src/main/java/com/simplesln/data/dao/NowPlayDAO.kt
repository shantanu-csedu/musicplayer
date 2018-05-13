package com.simplesln.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.NowPlay

@Dao
interface NowPlayDAO {
    @Query("delete from media_now_play")
    fun reset() : Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun set(nowPlay : NowPlay)

    @Query("select media_library.* from media_now_play left join media_queue on media_now_play.nowPlayId = media_queue.id left join media_library on media_library.id = media_queue.media_file_id limit 1")
    fun get() : LiveData<MediaFile>

    @Query("select media_library.* from media_now_play left join media_queue on media_now_play.nowPlayId = media_queue.id left join media_library on media_library.id = media_queue.media_file_id limit 1")
    fun getSync() : MediaFile
}