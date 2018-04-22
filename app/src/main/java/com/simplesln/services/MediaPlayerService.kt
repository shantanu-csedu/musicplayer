package com.simplesln.services

import android.app.Service
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.simplesln.data.entities.MediaFile
import com.simplesln.interfaces.DataProvider
import com.simplesln.interfaces.MediaPlayer
import android.support.v4.app.NotificationCompat
import android.app.Notification.MediaStyle
import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import android.util.Log
import com.simplesln.data.*
import com.simplesln.simpleplayer.R


class MediaPlayerService : LifecycleService(), MediaPlayer, android.media.MediaPlayer.OnCompletionListener, android.media.MediaPlayer.OnPreparedListener {
    override fun onPrepared(mp: android.media.MediaPlayer?) {
        if(mp != null){
            if(mp.isPlaying){
                liveMediaPlayerState.update(MediaPlayerState(STATE_PLAYING, mMediaFile))
            }
            else{
                liveMediaPlayerState.update(MediaPlayerState(STATE_IDLE, mMediaFile))
            }
        }
    }

    override fun onCompletion(mp: android.media.MediaPlayer?) {
        if(mp != null){
            if(mp.currentPosition == mp.duration){
                liveMediaPlayerState.update(MediaPlayerState(STATE_END, mMediaFile))
            }
            else if(mp.isLooping && mp.isPlaying){
                liveMediaPlayerState.update(MediaPlayerState(STATE_PLAYING, mMediaFile))
            }
            else{
                liveMediaPlayerState.update(MediaPlayerState(STATE_STOPPED, mMediaFile))
                handler.postDelayed({
                    next()
                },500)
            }
        }
    }

    lateinit var instance : MediaPlayerService
    private var player : android.media.MediaPlayer
    lateinit private var dataProvider : DataProvider
    private val CHANNEL_ID = "com.simplesln.simpler.player.notification"
    private val CHANNEL_NAME = "Simple Music Player"
    private var mPrepared = false
    private var liveMediaPlayerState : LiveMediaPlayerState
    private var mMediaFile : MediaFile? = null
    private var handler = Handler()

    init {
        player = android.media.MediaPlayer()
        player.setOnCompletionListener(this)
        player.setOnPreparedListener(this)
        liveMediaPlayerState = LiveMediaPlayerState()
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

    fun getMediaPlayerState() : LiveData<MediaPlayerState>{
        return liveMediaPlayerState
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
        dataProvider.getNowPlay().observe(this,object : Observer<MediaFile>{
            var mInit = false
            override fun onChanged(mediaFile: MediaFile?) {
                if(mediaFile != null) Log.e("Now play ", mediaFile.name)
                if(initPlayer(mediaFile)){
                    if(mInit) play()
                    mInit = true
                }
                mMediaFile = mediaFile
            }

        })
    }

    private fun initPlayer(mediaFile : MediaFile?) : Boolean{
        if(mediaFile == null) return false
        if(player.isPlaying) player.stop()
        mPrepared = false
        player.reset()
        player.setDataSource(mediaFile.link)
        return true
    }

    override fun play() {
        if(mMediaFile != null) {
            if (!mPrepared) {
                player.prepare()
                mPrepared = true
            }
            else{
                liveMediaPlayerState.update(MediaPlayerState(STATE_PLAYING, mMediaFile))
            }
            player.start()
        }
    }

    override fun stop() {
        liveMediaPlayerState.update(MediaPlayerState(STATE_STOPPED, mMediaFile))
        player.pause()
    }

    override fun next() {
        var nextLiveData = dataProvider.getNext();
        var nextDataObserver = object : Observer<MediaFile> {
            override fun onChanged(mediaFile: MediaFile?) {
                nextLiveData.removeObserver(this)
                if(mediaFile != null) {
                    dataProvider.setNowPlaying(mediaFile.id)
                }
            }
        }
        nextLiveData.observe(this,nextDataObserver)
    }

    override fun duration(): Int {
        if(mPrepared)
            return player.duration
        return 0;
    }

    override fun prev() {
        var prevLiveData = dataProvider.getPrev();
        var prevDataObserver = object : Observer<MediaFile> {
            override fun onChanged(mediaFile: MediaFile?) {
                prevLiveData.removeObserver(this)
                if(mediaFile != null) {
                    dataProvider.setNowPlaying(mediaFile.id)
                }
            }
        }
        prevLiveData.observe(this,prevDataObserver)
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

    override fun currentPosition(): Int {
        if(mPrepared)
            return player.currentPosition
        return 0;
    }
}