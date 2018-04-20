package com.simplesln.services

import android.annotation.SuppressLint
import android.app.Service
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.simplesln.data.MediaFile
import com.simplesln.data.RoomDataProvider
import com.simplesln.interfaces.DataProvider
import com.simplesln.interfaces.MediaPlayer

class MediaPlayerService : LifecycleService(), MediaPlayer{

    lateinit var instance : MediaPlayerService
    private var player : android.media.MediaPlayer
    lateinit private var dataProvider : DataProvider

    init {
        player = android.media.MediaPlayer()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        dataProvider = RoomDataProvider(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return MediaPlayerServiceBinder()
    }

    inner class MediaPlayerServiceBinder : Binder() {
        fun getMediaService() : MediaPlayerService{
            return instance
        }
    }

    fun notifyMediaSelectionChanged(){
        dataProvider.getNowPlay().observe(this, Observer {
            mediaFile ->
            if(initPlayer(mediaFile)) play()

        })
    }

    private fun initPlayer(mediaFile : MediaFile?) : Boolean{
        if(player.isPlaying) player.stop()
        player.reset()
        player.setDataSource(mediaFile?.link)
        return mediaFile != null
    }

    override fun play() {
        player.prepare()
        player.start()
    }

    override fun stop() {
        player.stop()
    }

    override fun next() {
        dataProvider.getNext().observe(this, Observer {
            mediaFile ->
            if(initPlayer(mediaFile)) play()
        })
    }

    override fun duration(): Int {
        return player.duration
    }

    override fun prev() {
        dataProvider.getPrev().observe(this, Observer {
            mediaFile ->
            if(initPlayer(mediaFile)) play()
        })
    }

    override fun seek(position: Int) {
        player.seekTo(position)
    }

    override fun shortForward() {
        var curPosition  = player.currentPosition
        curPosition += 5*1000
        if(curPosition > player.duration) curPosition = player.duration
        player.seekTo(curPosition)
    }

    override fun longForward() {
        var curPosition  = player.currentPosition
        curPosition += 10*1000
        if(curPosition > player.duration) curPosition = player.duration
        player.seekTo(curPosition)
    }

    override fun shortBackward() {
        var curPosition  = player.currentPosition
        curPosition -= 5*1000
        if(curPosition <  0) curPosition = 0
        player.seekTo(curPosition)
    }

    override fun longBackward() {
        var curPosition  = player.currentPosition
        curPosition -= 10*1000
        if(curPosition <  0) curPosition = 0
        player.seekTo(curPosition)
    }
}