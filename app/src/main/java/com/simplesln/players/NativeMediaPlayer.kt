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

package com.simplesln.players

import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.media.MediaPlayer
import android.support.v4.media.session.MediaSessionCompat
import com.simplesln.data.*
import com.simplesln.data.entities.MediaFile
import com.simplesln.helpers.MediaSessionHelper
import com.simplesln.interfaces.DataProvider

class NativeMediaPlayer(context : Context, lifecycleOwner: LifecycleOwner, dataProvider: DataProvider) : MyPlayer(lifecycleOwner,dataProvider), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private val player : android.media.MediaPlayer = android.media.MediaPlayer()
    private var mPrepared: Boolean = false
    private val liveMediaPlayerState = LiveMediaPlayerState()
    private val mediaSessionHelper = MediaSessionHelper(context,this)

    init {
        player.setOnCompletionListener(this)
        player.setOnPreparedListener(this)
    }

    override fun initPlayer(mediaFile : MediaFile?) : Boolean{
        if(player.isPlaying){
            player.stop()
            liveMediaPlayerState.update(STATE_STOPPED)
        }
        mPrepared = false
        player.reset()
        liveMediaPlayerState.update(MediaPlayerState(STATE_IDLE,mediaFile))
        if(mediaFile != null) {
            player.setDataSource(mediaFile.link)
            player.prepare()
            return true
        }
        return false
    }

    override fun isPlaying(): Boolean {
        return (liveMediaPlayerState.lastState.state == STATE_PLAYING)
    }

    override fun release() {
        player.release()
    }

    override fun currentPosition(): Int {
        if(mPrepared) return player.currentPosition
        return 0
    }

    override fun getState(): LiveMediaPlayerState {
        return liveMediaPlayerState
    }

    override fun play() {
        if(liveMediaPlayerState.lastState.mediaFile != null) {
            if(player.currentPosition == player.duration){
                player.seekTo(0)
            }
            player.start()
            liveMediaPlayerState.update(STATE_PLAYING)
        }
    }

    override fun stop() {
        player.pause()
        liveMediaPlayerState.update(STATE_STOPPED)
    }

    override fun duration(): Int {
        if(mPrepared)
            return player.duration
        return 0;
    }

    override fun seek(position: Int) {
        if(mPrepared)
            player.seekTo(position)
    }

    override fun shortForward() {
        if(mPrepared) {
            var curPosition = player.currentPosition
            curPosition += 5 * 1000
            if (curPosition > player.duration) curPosition = player.duration
            player.seekTo(curPosition)
        }
    }

    override fun longForward() {
        if(mPrepared) {
            var curPosition = player.currentPosition
            curPosition += 10 * 1000
            if (curPosition > player.duration) curPosition = player.duration
            player.seekTo(curPosition)
        }
    }

    override fun shortBackward() {
        if(mPrepared) {
            var curPosition = player.currentPosition
            curPosition -= 5 * 1000
            if (curPosition < 0) curPosition = 0
            player.seekTo(curPosition)
        }
    }

    override fun longBackward() {
        if(mPrepared) {
            var curPosition = player.currentPosition
            curPosition -= 10 * 1000
            if (curPosition < 0) curPosition = 0
            player.seekTo(curPosition)
        }
    }


    override fun onPrepared(mp: android.media.MediaPlayer?) {
        mPrepared = true
        if(player.isPlaying){
            liveMediaPlayerState.update(STATE_PLAYING)
        }
        else{
            liveMediaPlayerState.update(STATE_READY)
        }
    }

    override fun onCompletion(mp: android.media.MediaPlayer?) {
        liveMediaPlayerState.update(STATE_END)
    }

    override fun getMediaSession(): MediaSessionCompat {
        return mediaSessionHelper.mSession
    }
}