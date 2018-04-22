package com.simplesln.data

import com.simplesln.data.entities.MediaFile
val STATE_IDLE = 0
val STATE_PLAYING = 1
val STATE_STOPPED = 2
val STATE_END = 3
val STATE_ERROR = 4

class MediaPlayerState {
    var state = STATE_IDLE
    var mediaFile :MediaFile? = null

    constructor(state: Int, mediaFile: MediaFile?) {
        this.state = state
        this.mediaFile = mediaFile
    }
}