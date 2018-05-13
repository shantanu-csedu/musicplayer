package com.simplesln.players

import android.arch.lifecycle.LifecycleOwner
import android.media.MediaPlayer
import com.simplesln.data.*
import com.simplesln.data.entities.MediaFile
import com.simplesln.interfaces.DataProvider

class NativeMediaPlayer(lifecycleOwner: LifecycleOwner, dataProvider: DataProvider) : MyPlayer(lifecycleOwner,dataProvider), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private val player : android.media.MediaPlayer = android.media.MediaPlayer()
    private var mPrepared: Boolean = false
    private val liveMediaPlayerState = LiveMediaPlayerState()

    init {
        player.setOnCompletionListener(this)
        player.setOnPreparedListener(this)
    }

    override fun initPlayer(mediaFile : MediaFile?) : Boolean{
        if(mediaFile != null) {
            if(player.isPlaying) player.stop()
            mPrepared = false
            player.reset()
            player.setDataSource(mediaFile.link)
            player.prepare()
            liveMediaPlayerState.update(MediaPlayerState(STATE_IDLE,mediaFile))
            return true
        }
        return false
    }

    override fun isPlaying(): Boolean {
        return (liveMediaPlayerState.lastState.state == STATE_PLAYING)
    }

    override fun release() {
        player.release()
    }

    override fun currentPosition(): Int {
        if(mPrepared) return player.currentPosition
        return 0
    }

    override fun getState(): LiveMediaPlayerState {
        return liveMediaPlayerState
    }

    override fun play() {
        if(liveMediaPlayerState.lastState.mediaFile != null) {
            if(player.currentPosition == player.duration){
                player.seekTo(0)
            }
            player.start()
            liveMediaPlayerState.update(STATE_PLAYING)
        }
    }

    override fun stop() {
        player.pause()
        liveMediaPlayerState.update(STATE_STOPPED)
    }

    override fun duration(): Int {
        if(mPrepared)
            return player.duration
        return 0;
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


    override fun onPrepared(mp: android.media.MediaPlayer?) {
        mPrepared = true
        if(player.isPlaying){
            liveMediaPlayerState.update(STATE_PLAYING)
        }
        else{
            liveMediaPlayerState.update(STATE_READY)
        }
    }

    override fun onCompletion(mp: android.media.MediaPlayer?) {
        liveMediaPlayerState.update(STATE_END)
    }
}