package com.simplesln.helpers

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import com.simplesln.interfaces.Player

class AudioFocusHelper(val context : Context,val player : Player) : AudioManager.OnAudioFocusChangeListener {
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                player.play()
            }

            AudioManager.AUDIOFOCUS_LOSS -> if (player.isPlaying()) {
                player.stop()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (player.isPlaying()) player.stop()
        }
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mFocusRequest: AudioFocusRequest? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mPlaybackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            mFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mPlaybackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, Handler())
                    .build()
        }
    }

    fun requestFocus(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(mFocusRequest)
        }
        else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    fun removeFocus(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(mFocusRequest)
        }
        else {
            audioManager.abandonAudioFocus(this)
        }
    }
}