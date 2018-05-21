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

package com.simplesln.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

//@Entity(tableName = "media_library")
@Entity(tableName = "media_library",
        indices = [Index(value = ["link"],unique = true)])
class MediaFile(val link: String, val name: String, val duration: Int, artist: String?, genre: String?, album: String?, val folder: String, year: String?, val repeatCount: Int = 1, val art: String = "", @PrimaryKey(autoGenerate = true) var id: Long = 0L, val del: Boolean = false, //1 like, -1 dislike
                val favorite: Int = 0) {
    val artist : String = if(artist == null || artist.isEmpty()) "unknown" else artist
    val genre : String = if(genre == null || genre.isEmpty()) "unknown" else genre
    val album : String = if(album == null || album.isEmpty()) "unknown" else album
    val year : String = if(year == null || year.isEmpty()) "unknown" else year
}