package com.simplesln.data.dao

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.persistence.room.*
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.NowPlayingFile

@Dao
interface NowPlayingDAO {
    @Query("select media_library.* from media_library left join media_now_playing on media_now_playing.media_file_id = media_library.id order by media_now_playing.rank limit 1" )
    fun getFirstItem() : LiveData<MediaFile>

    @Query("select media_library.* from media_library left join media_now_playing on media_now_playing.media_file_id = media_library.id order by media_now_playing.rank desc limit 1" )
    fun getLastItem() : LiveData<MediaFile>

    @Query("select media_library.* from media_library  left join media_now_playing on media_now_playing.media_file_id = media_library.id where media_now_playing.nowPlaying = 1 limit 1" )
    fun getNowPlayingItem() : LiveData<MediaFile>

    @Query("select media_library.* from media_library  left join media_now_playing on media_now_playing.media_file_id = media_library.id where media_now_playing.rank > (select rank from media_now_playing where nowPlaying = 1 limit 1) order by media_now_playing.rank limit 1")
    fun getNext() : LiveData<MediaFile>

    @Query("select media_library.* from media_library  left join media_now_playing on media_now_playing.media_file_id = media_library.id where media_now_playing.rank < (select rank from media_now_playing where nowPlaying = 1 order by timestamp limit 1) order by media_now_playing.rank desc limit 1")
    fun getPrevious() : LiveData<MediaFile>

    @Query("update media_now_playing set nowPlaying = 0 where nowPlaying = 1")
    fun resetNowPlaying()

    @Query("update media_now_playing set nowPlaying = 1 where media_file_id = :mediaId")
    fun setNowPlaying(mediaId : Long)

    @Query("select media_library.* from media_now_playing  left join media_library on media_now_playing.media_file_id = media_library.id order by media_now_playing.rank")
    fun getNowPlayList() : LiveData<List<MediaFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(nowPlayList : List<NowPlayingFile>) : List<Long>

    @Query("delete from media_now_playing")
    fun delete() : Int

    @Query("delete from media_now_playing where media_file_id = :mediaId")
    fun delete(mediaId: Long)

    @Query("select max(rank) from media_now_playing")
    fun getMaxRank() : Double

    @Query("select * from media_now_playing")
    fun get() : LiveData<List<NowPlayingFile>>

}