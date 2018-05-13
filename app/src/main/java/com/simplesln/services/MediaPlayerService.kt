package com.simplesln.services

import android.app.Notification
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
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_LOSS
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.os.Bundle
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import com.simplesln.simpleplayer.MainActivity
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.media.*
import android.media.session.MediaSession
import android.support.v4.media.session.MediaSessionCompat
import com.simplesln.helpers.NOTIFICATION_ID
import com.simplesln.helpers.NotificationHelper


const val ACTION_PLAY = "action.play"
const val ACTION_NEXT = "action.next"
const val ACTION_PAUSE = "action.pause"
const val ACTION_PREV = "action.prev"
class MediaPlayerService : LifecycleService(), MediaPlayer, android.media.MediaPlayer.OnCompletionListener, android.media.MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AUDIOFOCUS_GAIN -> {
                play()
            }

            AUDIOFOCUS_LOSS -> if (isPlaying()) {
                stop()
            }

            AUDIOFOCUS_LOSS_TRANSIENT -> if (isPlaying()) stop()
        }
    }

    override fun onPrepared(mp: android.media.MediaPlayer?) {
        if(mp != null){
            mPrepared = true
            if(mp.isPlaying){
                liveMediaPlayerState.update(MediaPlayerState(STATE_PLAYING, mMediaFile))
            }
            else{
                liveMediaPlayerState.update(MediaPlayerState(STATE_READY, mMediaFile))
            }
            startForeground(NOTIFICATION_ID,notificationHelper.createNotification(liveMediaPlayerState.lastState!!))
        }
    }

    override fun onCompletion(mp: android.media.MediaPlayer?) {
        if(mp != null){
            if(mp.currentPosition == mp.duration){
                liveMediaPlayerState.update(MediaPlayerState(STATE_END, mMediaFile))
                stopForeground(false)
            }
            else if(mp.isLooping && mp.isPlaying){
                liveMediaPlayerState.update(MediaPlayerState(STATE_PLAYING, mMediaFile))
            }
            else{
                liveMediaPlayerState.update(MediaPlayerState(STATE_STOPPED, mMediaFile))
                stopForeground(false)
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
            notificationHelper.updateNotification(liveMediaPlayerState.lastState!!)
        }
    }

    lateinit var instance : MediaPlayerService
    private var player : android.media.MediaPlayer = android.media.MediaPlayer()
    private lateinit var dataProvider : DataProvider

    private var mPrepared = false
    private var liveMediaPlayerState : LiveMediaPlayerState = LiveMediaPlayerState()
    private var mMediaFile : MediaFile? = null
    private var handler = Handler()
    private var mInit: Boolean = false
    private var mRepeatCount = 1
    private lateinit var notificationHelper : NotificationHelper

    init {
        player.setOnCompletionListener(this)
        player.setOnPreparedListener(this)
    }


    private lateinit var audioManager: AudioManager

    private var mFocusRequest: AudioFocusRequest? = null

    private lateinit var mSession: MediaSessionCompat

    private class MediaSessionCallback : MediaSessionCompat.Callback(){

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        notificationHelper = NotificationHelper(this)
        dataProvider = RoomDataProvider(this)

        liveMediaPlayerState.update(MediaPlayerState(STATE_STOPPED, mMediaFile))

        observeNowPlaying()

        handler.postDelayed({
            mInit = true
        },1000)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mPlaybackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            mFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mPlaybackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, handler)
                    .build()
        }
    }

    fun getMediaPlayerState() : LiveMediaPlayerState{
        return liveMediaPlayerState
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val filter = IntentFilter()
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PREV);
        registerReceiver(playerActionHandler, filter);

//        startForeground(NOTIFICATION_ID,createNotification())
        return Service.START_NOT_STICKY
    }


    private val playerActionHandler = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                ACTION_NEXT ->
                        next()
                ACTION_PAUSE ->
                        stop()
                ACTION_PLAY ->
                        play()
                ACTION_PREV ->
                        prev()
            }
        }
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
            notificationHelper.updateNotification(liveMediaPlayerState.lastState!!)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(mFocusRequest)
        }
        else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        startForeground(NOTIFICATION_ID,notificationHelper.createNotification(liveMediaPlayerState.lastState!!))
    }

    override fun stop() {
        liveMediaPlayerState.update(MediaPlayerState(STATE_STOPPED, mMediaFile))
        player.pause()
        notificationHelper.updateNotification(liveMediaPlayerState.lastState!!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(mFocusRequest)
        }
        else {
            audioManager.abandonAudioFocus(this)
        }
        stopForeground(false)
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
        unregisterReceiver(playerActionHandler)
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