package com.simplesln.data

import android.arch.lifecycle.MutableLiveData

class LiveMediaPlayerState : MutableLiveData<MediaPlayerState>() {
    var lastState : MediaPlayerState? = null
    fun update(mediaPlayerState: MediaPlayerState){
        lastState = mediaPlayerState
        value = mediaPlayerState
    }
}