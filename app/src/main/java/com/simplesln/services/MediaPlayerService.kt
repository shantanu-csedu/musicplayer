package com.simplesln.services

import android.app.Service
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.RoomDataProvider
import com.simplesln.interfaces.DataProvider
import com.simplesln.interfaces.MediaPlayer
import android.support.v4.app.NotificationCompat
import android.app.Notification.MediaStyle
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.simplesln.simpleplayer.R


class MediaPlayerService : LifecycleService(), MediaPlayer{

    lateinit var instance : MediaPlayerService
    private var player : android.media.MediaPlayer
    lateinit private var dataProvider : DataProvider
    private val CHANNEL_ID = "com.simplesln.simpler.player.notification"
    private val CHANNEL_NAME = "Simple Music Player"
    private var mPrepared = false
    init {
        player = android.media.MediaPlayer()
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
        dataProvider = RoomDataProvider(this)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW))
        }
        observeNowPlaying()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                // Add media control buttons that invoke intents in your media service
//                .addAction(R.drawable.ic_prev, "Previous", prevPendingIntent) // #0
//                .addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)  // #1
//                .addAction(R.drawable.ic_next, "Next", nextPendingIntent)     // #2
                // Apply the media style template
//                .setStyle(NotificationCompat.MediaStyle()
//                        .setShowActionsInCompactView(1 /* #1: pause button */)
//                        .setMediaSession(mMediaSession.getSessionToken()))
                .setContentTitle("Wonderful music")
                .setContentText("My Awesome Band")
//                .setLargeIcon(albumArtBitmap)
                .build()
        startForeground(2234,notification)
        return Service.START_NOT_STICKY
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

    private fun observeNowPlaying(){
        dataProvider.getNowPlay().observe(this, Observer {
            mediaFile ->
            if(mediaFile != null) Log.e("Now play ", mediaFile?.name)
            if(initPlayer(mediaFile)) play()
        })
    }

    private fun initPlayer(mediaFile : MediaFile?) : Boolean{
        if(mediaFile == null) return false
        if(player.isPlaying) player.stop()
        mPrepared = false
        player.reset()
        player.setDataSource(mediaFile?.link)
        return mediaFile != null
    }

    override fun play() {
//        dataProvider.getNowPlayList2().observe(this@MediaPlayerService, Observer {
//            list ->
//            if(list != null){
//                for(item in list){
//                    Log.e("now playing"," " + item.media_file_id + " " + item.nowPlaying)
//                }
//            }
//        })
        if(!mPrepared) {
            player.prepare()
            mPrepared = true
        }
        player.start()
    }

    override fun stop() {
        player.pause()
    }

    override fun next() {
        var nextLiveData = dataProvider.getNext();
        var nextDataObserver = object : Observer<MediaFile> {
            override fun onChanged(mediaFile: MediaFile?) {
                nextLiveData.removeObserver(this)
                if(mediaFile != null) {
                    dataProvider.setNowPlaying(mediaFile!!.id)
                }
            }
        }
        nextLiveData.observe(this,nextDataObserver)
    }

    override fun duration(): Int {
        return player.duration
    }

    override fun prev() {
        var prevLiveData = dataProvider.getPrev();
        var prevDataObserver = object : Observer<MediaFile> {
            override fun onChanged(mediaFile: MediaFile?) {
                prevLiveData.removeObserver(this)
                if(mediaFile != null) {
                    dataProvider.setNowPlaying(mediaFile!!.id)
                }
//                else{
//                    dataProvider.getNowPlayList2().observe(this@MediaPlayerService, Observer {
//                        list ->
//                        if(list != null){
//                            for(item in list){
//                                Log.e("now playing"," " + item.media_file_id + " " + item.nowPlaying)
//                            }
//                        }
//                    })
//                }
            }
        }
        prevLiveData.observe(this,prevDataObserver)
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

    override fun onDestroy() {
        super.onDestroy()
        if(player.isPlaying) player.stop()
        player.release()
    }

    override fun isPlaying(): Boolean {
        return player.isPlaying
    }
}