package com.simplesln.interfaces

import android.arch.lifecycle.LiveData
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.NowPlayingFile
import com.simplesln.data.entities.PlayList

interface DataProvider {
    fun getNowPlayList() : LiveData<List<MediaFile>>
    fun getNowPlayList2() : LiveData<List<NowPlayingFile>>
    fun getNowPlay() : LiveData<MediaFile>
    fun getNext() : LiveData<MediaFile>
    fun getPrev() : LiveData<MediaFile>
    fun addNowPlaying(index : Int, file : MediaFile)
    fun addNowPlaying(files : List<MediaFile>,clear : Boolean) : LiveData<Boolean>
    fun addMedia(files : List<MediaFile>)
    fun setNowPlaying(mediaId: Long)
    fun getMediaFiles(offset : Int,total : Int) : LiveData<List<MediaFile>>
    fun getAlbumList() : LiveData<List<String>>
    fun remove() : LiveData<Int>
    fun remove(mediaId: Long)
    fun removeNowPlaying() : LiveData<Int>
    fun removeNowPlaying(mediaId : Long)

    fun getArtistList(): LiveData<List<String>>
    fun getGenreList(): LiveData<List<String>>
    fun getPlayList(): LiveData<List<PlayList>>
    fun getMediaFilesByAlbum(name: String): LiveData<List<MediaFile>>
    fun getMediaFilesByArtist(name: String): LiveData<List<MediaFile>>
    fun getMediaFilesByGenre(name: String): LiveData<List<MediaFile>>
    fun getMediaFilesByPlaylist(name: String): LiveData<List<MediaFile>>
    fun getMediaFiles(): LiveData<List<MediaFile>>

    fun getRank(fromId : Long, toId : Long) : LiveData<Double>
    fun getRank(id : Long, before : Boolean) : LiveData<Double>

    fun updateRank(file : MediaFile, rank : Double)

    fun updateMediaFile(file : MediaFile)
    fun createPlaylist(name: String): LiveData<Long>
    fun addToPlayList(playlistName :String,mediaFiles:List<MediaFile>)
    fun setNext(id: Long)
}