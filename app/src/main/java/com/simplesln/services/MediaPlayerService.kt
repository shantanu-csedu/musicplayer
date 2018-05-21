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

package com.simplesln.services

import android.app.Service
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import com.simplesln.data.*
import com.simplesln.data.entities.MediaFile
import com.simplesln.interfaces.DataProvider
import com.simplesln.interfaces.Player
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.analytics.HitBuilders
import com.simplesln.helpers.AudioFocusHelper
import com.simplesln.helpers.MediaSessionHelper
import com.simplesln.helpers.NOTIFICATION_ID
import com.simplesln.helpers.NotificationHelper
import com.simplesln.players.NativeMediaPlayer
import com.simplesln.simpleplayer.createPlayer
import com.simplesln.simpleplayer.getDataProvider
import com.simplesln.simpleplayer.getDefaultTracker


const val ACTION_PLAY = "action.play"
const val ACTION_NEXT = "action.next"
const val ACTION_PAUSE = "action.pause"
const val ACTION_PREV = "action.prev"
class MediaPlayerService : LifecycleService(){

    lateinit var instance : MediaPlayerService
    private lateinit var player : Player
    private lateinit var dataProvider : DataProvider

    private var handler = Handler()
    private var mInit: Boolean = false
    private var mRepeatCount = 1
    private lateinit var notificationHelper : NotificationHelper
    private lateinit var audioFocusHelper : AudioFocusHelper
    private var mMediaFile: MediaFile? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        dataProvider = getDataProvider(this)
        player = createPlayer(this,this)
        notificationHelper = NotificationHelper(this, player.getMediaSession())
        audioFocusHelper = AudioFocusHelper(this,player)
        observeNowPlaying()
        observePlayerState()
        handler.postDelayed({
            mInit = true
        },1000)
    }

    fun getMediaPlayerState() : LiveMediaPlayerState{
        return player.getState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val filter = IntentFilter()
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PREV);
        registerReceiver(playerActionHandler, filter);
        return Service.START_NOT_STICKY
    }


    private val playerActionHandler = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                ACTION_NEXT -> {
                    try {
                        //sending analytics
                        getDefaultTracker(this@MediaPlayerService).send(HitBuilders.EventBuilder()
                                .setCategory("Notification")
                                .setAction("Click")
                                .setLabel("next")
                                .build())
                    }catch (ex : Exception){
                        ex.printStackTrace()
                    }
                    player.next()
                }
                ACTION_PAUSE -> {
                    try {
                        //sending analytics
                        getDefaultTracker(this@MediaPlayerService).send(HitBuilders.EventBuilder()
                                .setCategory("Notification")
                                .setAction("Click")
                                .setLabel("stop")
                                .build())
                    }catch (ex : Exception){
                        ex.printStackTrace()
                    }
                    player.stop()
                }
                ACTION_PLAY -> {
                    try {
                        //sending analytics
                        getDefaultTracker(this@MediaPlayerService).send(HitBuilders.EventBuilder()
                                .setCategory("Notification")
                                .setAction("Click")
                                .setLabel("play")
                                .build())
                    }catch (ex : Exception){
                        ex.printStackTrace()
                    }
                    player.play()
                }
                ACTION_PREV -> {
                    try {
                        //sending analytics
                        getDefaultTracker(this@MediaPlayerService).send(HitBuilders.EventBuilder()
                                .setCategory("Notification")
                                .setAction("Click")
                                .setLabel("previous")
                                .build())
                    }catch (ex : Exception){
                        ex.printStackTrace()
                    }
                    player.prev()
                }
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

    fun getPlayer() : Player{ return  player}



    private fun observeNowPlaying(){
        dataProvider.getNowPlay().observe(this, Observer {
            if(mMediaFile == null ||
                    !player.isPlaying() ||
                    mMediaFile?.id != it?.id) {
                mMediaFile = it
                if (player.initPlayer(it)) {
                    if (mInit){
                        mRepeatCount = 1
                        player.play()
                    }
                    mInit = true

                    try {
                        //sending analytics
                        getDefaultTracker(this@MediaPlayerService).send(HitBuilders.EventBuilder()
                                .setCategory("Playing")
                                .setAction("Change")
                                .setLabel("music")
                                .build())
                    }catch (ex : Exception){
                        ex.printStackTrace()
                    }
                }
            }
            else{
                mMediaFile = it
            }
        })
    }

    private fun observePlayerState(){
        player.getState().observe(this, Observer {
            when(it?.state){
                STATE_PLAYING -> {
                    audioFocusHelper.requestFocus()
                    startForeground(NOTIFICATION_ID,notificationHelper.createNotification(it))
                }
                STATE_IDLE -> {
                    if(it.mediaFile == null) stopForeground(true)
                }
                STATE_READY -> {

                }

                STATE_STOPPED ->{
                    notificationHelper.updateNotification(it)
                    audioFocusHelper.removeFocus()
                    stopForeground(it.mediaFile == null)
                }

                STATE_END ->{
                    notificationHelper.updateNotification(it)
                    audioFocusHelper.removeFocus()
                    stopForeground(false)

                    handler.postDelayed({
                        if(mMediaFile != null && mRepeatCount < mMediaFile?.repeatCount!!){
                            mRepeatCount++
                            player.play()
                        }
                        else {
                            player.next()
                        }
                    },500)
                }

                STATE_ERROR ->{
                    notificationHelper.updateNotification(it)
                    audioFocusHelper.removeFocus()
                    stopForeground(false)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if(player.isPlaying()) player.stop()
        player.release()
        notificationHelper.mediaSession.isActive = false
        notificationHelper.mediaSession.release()
        try {
            unregisterReceiver(playerActionHandler)
        }catch (ex : Exception){

        }

    }
}