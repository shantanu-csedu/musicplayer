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
                startForeground(NOTIFICATION_ID,createNotification())
            }
            else{
                liveMediaPlayerState.update(MediaPlayerState(STATE_READY, mMediaFile))
            }
            updateNotification()
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
            updateNotification()
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
    private val NOTIFICATION_ID = 982734

    init {
        player.setOnCompletionListener(this)
        player.setOnPreparedListener(this)
    }


    private lateinit var audioManager: AudioManager

    private var mFocusRequest: AudioFocusRequest? = null

    private lateinit var mSession: MediaSessionCompat

    private class MediaSessionCallback : MediaSessionCompat.Callback(){

    }
    private val REQUEST_MEDIA_CONTROL = 98
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        mSession = MediaSessionCompat(this, resources.getString(R.string.app_name))
        mSession.setCallback(MediaSessionCallback())
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, REQUEST_MEDIA_CONTROL,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        mSession.setSessionActivity(pi)
        dataProvider = RoomDataProvider(this)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW))
        }
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
    val ACTION_PLAY = "action.play"
    val ACTION_NEXT = "action.next"
    val ACTION_PAUSE = "action.pause"
    val ACTION_PREV = "action.prev"
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

    private fun createNotification(): Notification? {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)


        var fetchArtUrl: String? = null
        var art: Bitmap? = null
        if (mMediaFile?.art != null) {
            val artUrl = mMediaFile?.art
            fetchArtUrl = artUrl
            art = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        }

        addPrevAction(notificationBuilder)
        addPlayPauseAction(notificationBuilder)
        addNextAction(notificationBuilder)
        notificationBuilder
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mSession.sessionToken))
                .setColor(resources.getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.abc_btn_check_material)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent()) // Create an intent that would open the UI when user clicks the notification
                .setContentTitle(mMediaFile?.name)
                .setContentText(mMediaFile?.artist)
                .setLargeIcon(art)

//        if (fetchArtUrl != null) {
//            fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder)
//        }

        return notificationBuilder.build()
    }

    private fun updateNotification(){
        notificationManager.notify(NOTIFICATION_ID, createNotification());
    }


    private fun addPlayPauseAction(builder : NotificationCompat.Builder) {
        var icon =0
        var label = ""
        var intent : PendingIntent? = null
        if (isPlaying()) {
            icon = R.mipmap.ic_pause;
            label = "Pause"
            intent = PendingIntent.getBroadcast(this,0,Intent(ACTION_PAUSE),PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            icon = R.mipmap.ic_play;
            label = "Play"
            intent = PendingIntent.getBroadcast(this,0,Intent(ACTION_PLAY),PendingIntent.FLAG_UPDATE_CURRENT)
        }
        builder.addAction(NotificationCompat.Action(icon, label, intent));
    }

    private fun addNextAction(builder: NotificationCompat.Builder){
        builder.addAction(NotificationCompat.Action(R.mipmap.ic_next,"Next",PendingIntent.getBroadcast(this,0,Intent(ACTION_NEXT.toString()),PendingIntent.FLAG_UPDATE_CURRENT)))
    }

    private fun addPrevAction(builder: NotificationCompat.Builder){
        builder.addAction(NotificationCompat.Action(R.mipmap.ic_prev,"Previous",PendingIntent.getBroadcast(this,0,Intent(ACTION_PREV),PendingIntent.FLAG_UPDATE_CURRENT)))
    }


    private fun createContentIntent() : PendingIntent{
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        return PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
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
            updateNotification()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(mFocusRequest)
        }
        else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        startForeground(NOTIFICATION_ID,createNotification())
    }

    override fun stop() {
        liveMediaPlayerState.update(MediaPlayerState(STATE_STOPPED, mMediaFile))
        player.pause()
        updateNotification()
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