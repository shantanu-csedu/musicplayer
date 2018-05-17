/*
 * Copyright (c) 2018.  shantanu saha <shantanu.csedu@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.simplesln.repositories

import android.arch.lifecycle.LiveData
import android.content.Context
import com.simplesln.data.MyDB
import com.simplesln.data.QueryExecutor
import com.simplesln.data.entities.*
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.PlayList
import com.simplesln.data.getMyDBInstance
import com.simplesln.interfaces.DataProvider
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RoomDataProvider(context : Context) : DataProvider{


    private var db : MyDB? = getMyDBInstance(context)
    private var executorService : ExecutorService = Executors.newFixedThreadPool(3)

    override fun getNowPlay(): LiveData<MediaFile> {
        return db?.nowPlay()!!.get()
    }

    override fun getQueue(): LiveData<List<MediaFile>> {
        return db?.queue()!!.getQueue()
    }

    override fun getNext(): LiveData<MediaFile> {
        return QueryExecutor(executorService, Callable<MediaFile> {
            var mediaFile = db?.queue()?.getNext()
            if (mediaFile == null) { //end of queue or no item
                mediaFile = db?.queue()?.getFirst()
            }
            mediaFile
        })
    }

    override fun getPrev(): LiveData<MediaFile> {
        return QueryExecutor(executorService, Callable<MediaFile> {
            db?.queue()!!.getPrevious()
        })
    }

    override fun addMedia(files: List<MediaFile>) {
        executorService.submit({
            for(file in files){
                db?.library()?.insert(file)
            }
        })
    }

    override fun addNowPlaying(files: List<MediaFile>,clear : Boolean) : LiveData<Boolean>{
        return QueryExecutor(executorService, Callable<Boolean> {
            var rank = 0.0
            if (clear) {
                db?.queue()!!.delete()
            } else {
                rank = db?.queue()!!.getMaxRank() + 1
            }
            for ((index, file) in files.withIndex()) {
                db?.queue()?.insert(MediaQueue(file.id, rank + index))
            }
            true
        })
    }


    override fun remove() : LiveData<Int>{
        return QueryExecutor(executorService, Callable<Int> { db?.library()?.delete()!! })
    }

    override fun removeQueue(mediaId: Long) {
        executorService.submit({
            val npId = db?.nowPlay()?.getMediaFileId()
            if(mediaId == npId){//deleted item is currently playing, need to set another now playing
                var nextFile = db?.queue()?.getNext()
                if(nextFile == null){
                    nextFile = db?.queue()?.getPrevious()
                }
                if(nextFile != null){//only 1 file
                    setNowPlaying(nextFile.id)
                }
                else{
                    db?.nowPlay()?.reset()
                }
            }
            db?.queue()?.delete(mediaId)
        })
    }

    override fun remove(mediaId: Long){
        executorService.submit({
            db?.library()?.delete(mediaId)
        })
    }


    override fun setNowPlaying(mediaId: Long) {
        QueryExecutor(executorService, Callable<Void> {
            db?.nowPlay()!!.reset()
            db?.nowPlay()!!.set(
                    NowPlay(0L, db?.queue()!!.getId(mediaId)))
            null
        })
    }

    override fun getAlbumList(): LiveData<List<String>> {
        return db?.library()!!.getAlbum()
    }

    override fun getArtistList(): LiveData<List<String>> {
        return db?.library()!!.getArtist()
    }

    override fun getGenreList(): LiveData<List<String>> {
        return db?.library()!!.getGenre()
    }

    override fun getPlayList(): LiveData<List<PlayList>> {
        return db?.playlist()!!.getPlaylist()
    }

    override fun getMediaFilesByAlbum(name: String) : LiveData<List<MediaFile>> {
        return db?.library()!!.getMediaListByAlbum(name)
    }

    override fun getMediaFiles(): LiveData<List<MediaFile>> {
        return db?.library()!!.get()
    }

    override fun getMediaFilesByArtist(name: String): LiveData<List<MediaFile>> {
        return db?.library()!!.getMediaListByArtist(name)
    }

    override fun getMediaFilesByGenre(name: String): LiveData<List<MediaFile>> {
        return db?.library()!!.getMediaListByGenre(name)
    }

    override fun getMediaFilesByPlaylist(name: String): LiveData<List<MediaFile>> {
        return db?.playlist()!!.getMediaFiles(name)
    }

    override fun getRank(fromId: Long, toId: Long): LiveData<Double> {
        return QueryExecutor(executorService, Callable<Double> {
            db?.queue()!!.getAvgRank(fromId, toId)
        })
    }

    override fun getRank(id: Long, before: Boolean): LiveData<Double> {
        return QueryExecutor(executorService, Callable<Double> {
            if (before) db?.queue()!!.getRank(id) - 1
            else db?.queue()!!.getRank(id) + 1
        })
    }

    override fun updateRank(file: MediaFile, rank: Double) {
        QueryExecutor(executorService, Callable<Void> {
            val npid = db?.queue()?.getId(file.id)
            val nowPlayingFile = MediaQueue(file.id, rank)
            nowPlayingFile.id = if (npid != null) npid else 0L
            if (nowPlayingFile.id > 0) {
                db?.queue()?.update(Arrays.asList(nowPlayingFile))
            }
            null
        })
    }

    override fun updateMediaFile(file: MediaFile) {
        QueryExecutor(executorService, Callable<Void> {
            db?.library()?.update(Arrays.asList(file))
            null
        })
    }

    override fun createPlaylist(name: String): LiveData<Long> {
        return QueryExecutor(executorService, Callable<Long> {
            val playList = PlayList(name)
            db?.playlist()?.insert(playList)
        })
    }

    override fun addToPlayList(name: String, mediaFiles: List<MediaFile>) {
        QueryExecutor(executorService, Callable<Void> {
            var playlistId = db?.playlist()?.getPlaylistId(name)
            if (playlistId!! <= 0L) {//doesn't exits
                playlistId = db?.playlist()?.insert(PlayList(name))
            }
            for (mediaFile in mediaFiles) {
                val playlistData = PlayListData(mediaFile.id, playlistId!!)
                db?.playlist()?.insertPlayListData(playlistData)
            }
            null
        })
    }

    override fun setNext(id: Long) {
        QueryExecutor(executorService, Callable<Void> {
            val nowPlayingItem = db?.nowPlay()?.getSync()
            val nextPlayingItem = db?.queue()?.getNext()
            val mediaFile = db?.queue()?.get(id)
            if (mediaFile == null) { //insert
                if (nowPlayingItem == null) {
                    db?.queue()?.insert(MediaQueue(id, 0.0))
                } else if (nextPlayingItem == null) { //no next item
                    db?.queue()?.insert(MediaQueue(id, db?.queue()?.getRank(nowPlayingItem.id)!! + 1))
                } else {
                    db?.queue()?.insert(MediaQueue(id, db?.queue()?.getAvgRank(nowPlayingItem.id, nextPlayingItem.id)!!))
                }
            } else { //update
                if (nowPlayingItem == null) {
                    mediaFile.rank = 0.0
                    db?.queue()?.update(Arrays.asList(mediaFile))
                } else if (nextPlayingItem == null) { //no next item
                    mediaFile.rank = db?.queue()?.getRank(nowPlayingItem.id)!! + 1
                    db?.queue()?.update(Arrays.asList(mediaFile))
                } else {
                    mediaFile.rank = db?.queue()?.getAvgRank(nowPlayingItem.id, nextPlayingItem.id)!!
                    db?.queue()?.update(Arrays.asList(mediaFile))
                }
            }
            null
        })
    }

    override fun removeFromPlaylist(mediaId: Long, playlistName: String) {
        QueryExecutor(executorService,Callable<Void>{
            db?.playlist()?.deleteMusic(mediaId,playlistName)
            null
        })
    }

    override fun removePlaylist(name: String) {
        QueryExecutor(executorService,Callable<Void>{
            db?.playlist()?.deletePlaylist(name)
            null
        })
    }
}