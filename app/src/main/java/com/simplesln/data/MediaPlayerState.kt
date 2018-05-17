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
val STATE_IDLE = 0
val STATE_PLAYING = 1
val STATE_STOPPED = 2
val STATE_END = 3
val STATE_ERROR = 4
val STATE_READY = 5

class MediaPlayerState {
    var state = STATE_IDLE
    var mediaFile :MediaFile? = null

    constructor(state: Int, mediaFile: MediaFile?) {
        this.state = state
        this.mediaFile = mediaFile
    }
}