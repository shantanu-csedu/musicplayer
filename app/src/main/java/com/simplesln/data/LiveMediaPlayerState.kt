package com.simplesln.data

import android.arch.lifecycle.LiveData

class LiveMediaPlayerState : LiveData<MediaPlayerState>() {
    fun update(mediaPlayerState: MediaPlayerState){
        value = mediaPlayerState
    }

    fun post(mediaPlayerState: MediaPlayerState){
        postValue(mediaPlayerState)
    }
}