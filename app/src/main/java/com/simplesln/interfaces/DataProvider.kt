package com.simplesln.interfaces

import android.arch.lifecycle.LiveData
import com.simplesln.data.entities.MediaFile

interface DataProvider {
    fun getNowPlayList() : LiveData<List<MediaFile>>
    fun getNowPlay() : LiveData<MediaFile>
    fun getNext() : LiveData<MediaFile>
    fun getPrev() : LiveData<MediaFile>
    fun addNowPlaying(index : Int, file : MediaFile)
    fun addNowPlaying(files : List<MediaFile>,nowPlayingId : Long)
    fun addMedia(files : List<MediaFile>)
    fun remove()
    fun remove(mediaId: Long)
    fun removeNowPlaying()
    fun removeNowPlaying(mediaId : Long)
}