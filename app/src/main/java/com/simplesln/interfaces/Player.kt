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