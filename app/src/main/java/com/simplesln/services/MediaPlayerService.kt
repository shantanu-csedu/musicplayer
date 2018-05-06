package com.simplesln.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.simplesln.data.*
import com.simplesln.data.entities.MediaFile
import com.simplesln.interfaces.DataProvider
import com.simplesln.interfaces.MediaPlayer
import com.simplesln.simpleplayer.R


class MediaPlayerService : LifecycleService(), MediaPlayer, android.media.MediaPlayer.OnCompletionListener, android.media.MediaPlayer.OnPreparedListener {
    override fun onPrepared(mp: android.media.MediaPlayer?) {
        if(mp != null){
            mPrepared = true
            if(mp.isPlaying){
                liveMediaPlayerState.update(MediaPlayerState(STATE_PLAYING, mMediaFile))
            }
            else{
                liveMediaPlayerState.update(MediaPlayerState(STATE_READY, mMediaFile))
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
                    if(mMediaFile != null && mRepeatCount < mMediaFile?.repeatCount!!){
                        mRepeatCount++
                        play()
                    }
                    else {
                        next()
                    }
                },500)
            }
        }
    }

    lateinit var instance : MediaPlayerService
    private var player : android.media.MediaPlayer = android.media.MediaPlayer()
    private lateinit var dataProvider : DataProvider
    private val CHANNEL_ID = "com.simplesln.simpler.player.notification"
    private val CHANNEL_NAME = "Simple Music Player"
    private var mPrepared = false
    private var liveMediaPlayerState : LiveMediaPlayerState = LiveMediaPlayerState()
    private var mMediaFile : MediaFile? = null
    private var handler = Handler()
    private var mInit: Boolean = false
    private var mRepeatCount = 1

    init {
        player.setOnCompletionListener(this)
        player.setOnPreparedListener(this)
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
        dataProvider = RoomDataProvider(this)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW))
        }
        liveMediaPlayerState.update(MediaPlayerState(STATE_STOPPED, mMediaFile))
        observeNowPlaying()
        handler.postDelayed({
            mInit = true
        },1000)

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
        dataProvider.getNowPlay().observe(this, Observer {
            if(mMediaFile == null || !isPlaying() || mMediaFile?.id != it?.id) {
                mMediaFile = it
                if (initPlayer(it)) {
                    if (mInit){
                        mRepeatCount = 1
                        play()
                    }
                    mInit = true
                }
            }
            else{
                mMediaFile = it
            }
            if(mMediaFile == null){
                stop()
            }
        })
    }

    private fun initPlayer(mediaFile : MediaFile?) : Boolean{
        if(mediaFile != null) {
            if(player.isPlaying) player.stop()
            mPrepared = false
            player.reset()
            player.setDataSource(mediaFile.link)
            player.prepare()
            return true
        }
        return false
    }

    override fun play() {
        if(mMediaFile != null) {
            liveMediaPlayerState.update(MediaPlayerState(STATE_PLAYING, mMediaFile))
            if(player.currentPosition == player.duration){
                player.seekTo(0)
            }
            player.start()
        }
    }

    override fun stop() {
        liveMediaPlayerState.update(MediaPlayerState(STATE_STOPPED, mMediaFile))
        player.pause()
    }

    override fun next() {
        val nextLiveData = dataProvider.getNext();
        val nextDataObserver = object : Observer<MediaFile> {
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
        val prevLiveData = dataProvider.getPrev();
        val prevDataObserver = object : Observer<MediaFile> {
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