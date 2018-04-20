package com.simplesln.interfaces

import android.arch.lifecycle.LiveData
import com.simplesln.data.MediaFile

interface DataProvider {
    fun getNowPlayList() : LiveData<List<MediaFile>>
    fun getNowPlay() : LiveData<MediaFile>
    fun getNext() : LiveData<MediaFile>
    fun getPrev() : LiveData<MediaFile>
    fun addMedia(index : Int, file : MediaFile)
    fun addMedia(files : List<MediaFile>)
    fun removeAll()
    fun remove(index : Int)
}