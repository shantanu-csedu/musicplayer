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
import com.simplesln.data.entities.PlayList
import com.simplesln.simpleplayer.R

class PlayList (name : String ,id : Long = 0, icon : Int = R.mipmap.ic_default_music){
    val name : String = name
    val icon : Int = icon
    val id : Long = id

    constructor(playListEntity : PlayList) : this(playListEntity.name,playListEntity.id) {

    }
}