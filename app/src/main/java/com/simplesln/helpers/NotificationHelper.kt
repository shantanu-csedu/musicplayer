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

const val NOTIFICATION_ID = 982734
class NotificationHelper(val context : Context) {
    private val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID = "com.simplesln.simpler.player.notification"
    private val CHANNEL_NAME = "Simple Music Player"
    private val REQUEST_MEDIA_CONTROL = 98

    private var mSession: MediaSessionCompat

    init {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }
        mSession = MediaSessionCompat(context, context.resources.getString(R.string.app_name))
//        mSession.setCallback(MediaPlayerService.MediaSessionCallback())
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, REQUEST_MEDIA_CONTROL,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        mSession.setSessionActivity(pi)
    }

    fun createNotification(playerState : MediaPlayerState): Notification? {
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        var fetchArtUrl: String? = null
        var art: Bitmap? = null
        if (playerState.mediaFile?.art != null) {
            val artUrl = playerState.mediaFile?.art
            fetchArtUrl = artUrl
            art = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        }

        addPrevAction(notificationBuilder)
        addPlayPauseAction(notificationBuilder,playerState)
        addNextAction(notificationBuilder)
        notificationBuilder
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mSession.sessionToken))
                .setColor(context.resources.getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.abc_btn_check_material)
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
        var icon =0
        var label = ""
        var intent : PendingIntent? = null
        if (playerState.state == STATE_PLAYING) {
            icon = R.mipmap.ic_pause;
            label = "Pause"
            intent = PendingIntent.getBroadcast(context,0, Intent(ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            icon = R.mipmap.ic_play;
            label = "Play"
            intent = PendingIntent.getBroadcast(context,0, Intent(ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT)
        }
        builder.addAction(NotificationCompat.Action(icon, label, intent));
    }

    private fun addNextAction(builder: NotificationCompat.Builder){
        builder.addAction(NotificationCompat.Action(R.mipmap.ic_next,"Next", PendingIntent.getBroadcast(context,0, Intent(ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT)))
    }

    private fun addPrevAction(builder: NotificationCompat.Builder){
        builder.addAction(NotificationCompat.Action(R.mipmap.ic_prev,"Previous", PendingIntent.getBroadcast(context,0, Intent(ACTION_PREV), PendingIntent.FLAG_UPDATE_CURRENT)))
    }
}