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

package com.simplesln.data

import com.simplesln.data.entities.MediaFile

class MediaFile(entity : MediaFile,playing : Boolean = false) {
    var id : Long = entity.id
    var link : String = entity.link
    var name : String = entity.name
    var duration : Int = entity.duration
    var artist : String = entity.artist
    var genre : String = entity.genre
    var album : String = entity.album
    var folder : String = entity.folder
    var year : String = entity.year
    var repeatCount : Int = entity.repeatCount
    var art : String = entity.art
    var playing = playing

    fun getEntity() : MediaFile{
        return MediaFile(link,name,duration,artist,genre,album,folder,year,repeatCount,art,id)
    }
}