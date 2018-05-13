package com.simplesln.data

import android.arch.lifecycle.MutableLiveData

class LiveMediaPlayerState : MutableLiveData<MediaPlayerState>() {
    var lastState : MediaPlayerState = MediaPlayerState(STATE_IDLE,null)
    fun update(mediaPlayerState: MediaPlayerState){
        lastState = mediaPlayerState
        value = mediaPlayerState
    }

    fun update(state: Int){
        update(MediaPlayerState(state,lastState.mediaFile))
    }
}