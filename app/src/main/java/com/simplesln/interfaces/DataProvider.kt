package com.simplesln.interfaces

import android.arch.lifecycle.LiveData
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.NowPlayingFile

interface DataProvider {
    fun getNowPlayList() : LiveData<List<MediaFile>>
    fun getNowPlayList2() : LiveData<List<NowPlayingFile>>
    fun getNowPlay() : LiveData<MediaFile>
    fun getNext() : LiveData<MediaFile>
    fun getPrev() : LiveData<MediaFile>
    fun addNowPlaying(index : Int, file : MediaFile)
    fun addNowPlaying(files : List<MediaFile>,nowPlayingId : Long) : LiveData<Boolean>
    fun addMedia(files : List<MediaFile>)
    fun setNowPlaying(mediaId: Long)
    fun getMediaFiles(offset : Int,total : Int) : LiveData<List<MediaFile>>
    fun remove() : LiveData<Int>
    fun remove(mediaId: Long)
    fun removeNowPlaying() : LiveData<Int>
    fun removeNowPlaying(mediaId : Long)
}