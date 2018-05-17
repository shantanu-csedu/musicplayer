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

package com.simplesln.helpers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.simplesln.interfaces.Player
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R

class MediaSessionHelper(val context : Context, val player : Player) : MediaSessionCompat.Callback() {
    private val REQUEST_MEDIA_CONTROL = 98
    val mSession: MediaSessionCompat = MediaSessionCompat(context, context.resources.getString(R.string.app_name))
    init {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, REQUEST_MEDIA_CONTROL,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        val builder = PlaybackStateCompat.Builder()
        builder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        mSession.setPlaybackState(builder.build())
        mSession.setSessionActivity(pi)
        mSession.setCallback(this)
        mSession.isActive = true

    }

    override fun onPause() {
        super.onPause()
        player.stop()
    }

    override fun onStop() {
        super.onStop()
        player.stop()
    }

    override fun onPlay() {
        super.onPlay()
        if(player.isPlaying())player.stop()
        else player.play()
    }

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
        player.seek(pos.toInt())
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        player.next()
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        player.prev()
    }
}