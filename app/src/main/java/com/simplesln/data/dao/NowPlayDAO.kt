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
import com.simplesln.data.entities.NowPlay

@Dao
interface NowPlayDAO {
    @Query("delete from media_now_play")
    fun reset() : Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun set(nowPlay : NowPlay) : Long

    @Query("update media_now_play set queueId=:queueId where id=:id")
    fun update(id:Long, queueId:Long) : Long

    @Query("select media_library.* from media_now_play left join media_queue on media_now_play.queueId = media_queue.id left join media_library on media_library.id = media_queue.media_file_id order by media_now_play.id desc limit 1")
    fun get() : LiveData<MediaFile>

    @Query("select * from media_now_play order by id desc limit 1")
    fun getNowPlay() : NowPlay

    @Query("select media_library.* from media_now_play left join media_queue on media_now_play.queueId = media_queue.id left join media_library on media_library.id = media_queue.media_file_id limit 1")
    fun getSync() : MediaFile

    @Query("select media_queue.media_file_id from media_now_play left join media_queue on media_now_play.queueId = media_queue.id limit 1")
    fun getMediaFileId() : Long
}