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

package com.simplesln.interfaces

import android.arch.lifecycle.LiveData
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.PlayList

interface DataProvider {
    fun getQueue() : LiveData<List<MediaFile>>
    fun getNowPlay() : LiveData<MediaFile>
    fun getNext() : LiveData<MediaFile>
    fun getPrev() : LiveData<MediaFile>
    fun addQueue(files : List<MediaFile>,clear : Boolean) : LiveData<Boolean>
    fun addQueue(file : MediaFile, rank: Double) : LiveData<Boolean>
    fun addMedia(files : List<MediaFile>)
    fun setNowPlaying(mediaId: Long)
    fun getAlbumList() : LiveData<List<String>>
    fun removeMedia() : LiveData<Int>
    fun removeMedia(mediaId: Long)
    fun removeQueue(mediaId : Long)
    fun removePlaylist(name : String)
    fun removeFromPlaylist(mediaId: Long,playlistName : String)

    fun getArtistList(): LiveData<List<String>>
    fun getGenreList(): LiveData<List<String>>
    fun getPlayList(): LiveData<List<PlayList>>
    fun getMediaFilesByAlbum(name: String): LiveData<List<MediaFile>>
    fun getMediaFilesByArtist(name: String): LiveData<List<MediaFile>>
    fun getMediaFilesByGenre(name: String): LiveData<List<MediaFile>>
    fun getMediaFilesByPlaylist(name: String): LiveData<List<MediaFile>>
    fun getMediaFiles(): LiveData<List<MediaFile>>

    fun getRank(fromId : Long, toId : Long) : LiveData<Double>
    fun getRank(id: Long): LiveData<Double>

    fun updateRank(file : MediaFile, rank : Double)

    fun updateMediaFile(file : MediaFile)
    fun createPlaylist(name: String): LiveData<Long>
    fun addToPlayList(name: String ,mediaFiles:List<MediaFile>)
    fun setNext(id: Long)
}