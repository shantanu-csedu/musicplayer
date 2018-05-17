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
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "media_now_play",
        foreignKeys = [
            (ForeignKey(
                entity = MediaQueue::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("nowPlayId"),
                onDelete = ForeignKey.CASCADE
        ))
        ],
        indices = [(Index(value = arrayOf("nowPlayId"), unique = true))]
)
class NowPlay(id: Long = 0, nowPlayId : Long) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = id
    var nowPlayId : Long = nowPlayId
}