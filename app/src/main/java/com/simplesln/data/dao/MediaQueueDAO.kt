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
import com.simplesln.data.entities.MediaQueue

@Dao
interface MediaQueueDAO {

    @Query("select media_library.* from media_queue  left join media_library on media_queue.media_file_id = media_library.id order by media_queue.rank")
    fun getQueue() : LiveData<List<MediaFile>>

    @Query("select media_library.* from media_queue  left join media_library on media_queue.media_file_id = media_library.id where media_queue.rank > (select media_queue.rank from media_now_play left join media_queue on media_queue.id = media_now_play.queueId limit 1) order by media_queue.rank limit 1")
    fun getNext() : MediaFile

    @Query("select media_library.* from media_queue left join media_library on media_queue.media_file_id = media_library.id order by media_queue.rank limit 1")
    fun getFirst() : MediaFile

    @Query("select media_library.* from media_queue  left join media_library on media_queue.media_file_id = media_library.id where media_queue.rank < (select media_queue.rank from media_now_play left join media_queue on media_queue.id = media_now_play.queueId limit 1) order by media_queue.rank desc limit 1")
    fun getPrevious() : MediaFile

    @Query("select id from media_queue where media_file_id = :mediaFileId")
    fun getId(mediaFileId: Long): Long

    @Query("select avg(rank) from media_queue where media_file_id = :fromId or media_file_id = :toId")
    fun getAvgRank(fromId: Long, toId: Long): Double

    @Query("select rank from media_queue where media_file_id = :id")
    fun getRank(id: Long): Double

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(nowPlayList : MediaQueue) : Long

    @Update
    fun update(nowPlayList : List<MediaQueue>)

    @Query("delete from media_queue")
    fun delete() : Int

    @Query("delete from media_queue where media_file_id = :mediaId")
    fun delete(mediaId: Long)

    @Query("select max(rank) from media_queue")
    fun getMaxRank() : Double

    @Query("select * from media_queue where media_file_id =:mediaFileId")
    fun get(mediaFileId : Long) : MediaQueue
}