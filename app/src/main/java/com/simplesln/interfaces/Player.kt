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

import android.support.v4.media.session.MediaSessionCompat
import com.simplesln.data.LiveMediaPlayerState
import com.simplesln.data.entities.MediaFile

interface Player {
    fun initPlayer(mediaFile : MediaFile?) : Boolean
    fun play()
    fun stop()
    fun next()
    fun prev()
    fun release()
    fun isPlaying() : Boolean
    fun duration() : Int
    fun currentPosition() : Int
    fun seek(position : Int)
    fun shortForward();
    fun longForward()
    fun shortBackward()
    fun longBackward()
    fun getMediaSession() : MediaSessionCompat
    fun getState() : LiveMediaPlayerState
}