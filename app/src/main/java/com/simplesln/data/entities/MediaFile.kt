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
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "media_library")
class MediaFile(link: String, name: String, duration: Int, artist: String?, genre: String?, album: String?, folder: String, year: String?,repeatCount : Int = 1,art : String = "", id : Long = 0L, del : Boolean = false, favorite : Int = 0) {
    @PrimaryKey(autoGenerate = true)
    var id : Long = id
    var link : String = link
    var name : String = name
    var duration : Int = duration
    var artist : String = if(artist == null || artist.isEmpty()) "unknown" else artist
    var genre : String = if(genre == null || genre.isEmpty()) "unknown" else genre
    var album : String = if(album == null || album.isEmpty()) "unknown" else album
    var folder : String = folder
    var year : String = if(year == null || year.isEmpty()) "unknown" else year
    var repeatCount : Int = repeatCount
    var art : String = art
    var del : Boolean = del
    val favorite : Int = favorite //1 like, -1 dislike
}