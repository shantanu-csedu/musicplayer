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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.media.session.MediaSessionCompat
import com.simplesln.data.MediaPlayerState
import com.simplesln.data.STATE_PLAYING
import com.simplesln.services.ACTION_NEXT
import com.simplesln.services.ACTION_PAUSE
import com.simplesln.services.ACTION_PLAY
import com.simplesln.services.ACTION_PREV
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R
import android.media.session.MediaSession
import android.support.v4.content.ContextCompat
import android.util.Base64
import java.io.ByteArrayInputStream


const val NOTIFICATION_ID = 982734
class NotificationHelper(val context : Context,val mediaSession : MediaSessionCompat) {
    private val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID = "com.simplesln.simpler.player.notification"
    private val CHANNEL_NAME = "Simple Music Player"
    init {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(playerState : MediaPlayerState): Notification? {
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        val art: Bitmap =
        if (playerState.mediaFile?.art!!.isEmpty()) {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_splash)
        }
        else{
            BitmapFactory.decodeStream(ByteArrayInputStream(Base64.decode(playerState.mediaFile?.art, Base64.DEFAULT)))
        }

        addPrevAction(notificationBuilder)
        addPlayPauseAction(notificationBuilder,playerState)
        addNextAction(notificationBuilder)
        notificationBuilder
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken).setShowActionsInCompactView(0,1,2))
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent()) // Create an intent that would open the UI when user clicks the notification
                .setContentTitle(playerState.mediaFile?.name)
                .setContentText(playerState.mediaFile?.artist)
                .setLargeIcon(art)

//        if (fetchArtUrl != null) {
//            fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder)
//        }

        return notificationBuilder.build()
    }

    fun updateNotification(playerState: MediaPlayerState){
        notificationManager.notify(NOTIFICATION_ID, createNotification(playerState));
    }

    private fun createContentIntent() : PendingIntent{
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        return PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }



    private fun addPlayPauseAction(builder : NotificationCompat.Builder,playerState: MediaPlayerState) {

        val label = if(playerState.state == STATE_PLAYING) "Pause" else "Play"
        val intent = if(playerState.state == STATE_PLAYING) PendingIntent.getBroadcast(context,0, Intent(ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT) else PendingIntent.getBroadcast(context,0, Intent(ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT)
        val icon = if(playerState.state == STATE_PLAYING) R.mipmap.ic_pause else R.mipmap.ic_play
        builder.addAction(NotificationCompat.Action(icon, label, intent));
    }

    private fun addNextAction(builder: NotificationCompat.Builder){
        builder.addAction(NotificationCompat.Action(R.mipmap.ic_next,"Next", PendingIntent.getBroadcast(context,0, Intent(ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT)))
    }

    private fun addPrevAction(builder: NotificationCompat.Builder){
        builder.addAction(NotificationCompat.Action(R.mipmap.ic_prev,"Previous", PendingIntent.getBroadcast(context,0, Intent(ACTION_PREV), PendingIntent.FLAG_UPDATE_CURRENT)))
    }
}