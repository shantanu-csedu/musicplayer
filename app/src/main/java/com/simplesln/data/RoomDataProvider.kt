package com.simplesln.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.util.Log
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.NowPlayingFile
import com.simplesln.data.entities.PlayList
import com.simplesln.interfaces.DataProvider
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RoomDataProvider(context : Context) : DataProvider{


    private var db : MyDB? = getInstance(context)
    private var executorService : ExecutorService = Executors.newFixedThreadPool(3)

    override fun getNowPlay(): LiveData<MediaFile> {
        val distinctLiveData = MediatorLiveData<MediaFile>()
        distinctLiveData.addSource(db?.nowPlaying()!!.getNowPlayingItem(), object : Observer<MediaFile> {
            private var initialized = false
            private var lastObj: MediaFile? = null
            override fun onChanged(obj: MediaFile?) {
                if(obj == null) return
                if (!initialized) {
                    initialized = true
                    lastObj = obj
                    distinctLiveData.postValue(lastObj)
                } else if (obj != lastObj) {
                    lastObj = obj
                    distinctLiveData.postValue(lastObj)
                }
            }
        })
        return distinctLiveData
    }

    override fun getNowPlayList(): LiveData<List<MediaFile>> {
        return db?.nowPlaying()!!.getNowPlayList()
    }

    override fun getNext(): LiveData<MediaFile> {
        return db?.nowPlaying()!!.getNext()
    }

    override fun getPrev(): LiveData<MediaFile> {
        return db?.nowPlaying()!!.getPrevious()
    }

    override fun addMedia(files: List<MediaFile>) {
        executorService.submit({
            db?.library()?.insert(files)
        })
    }

    override fun addNowPlaying(files: List<MediaFile>,clear : Boolean) : LiveData<Boolean>{
        return QueryExecutor(executorService, object : Callable<Boolean>{
            override fun call(): Boolean{
                var nowPlayingList  = ArrayList<NowPlayingFile>()
                var rank = db?.nowPlaying()!!.getMaxRank() + 1
                for(file in files){
                    var nowPlayingFile = NowPlayingFile(file.id,rank,false)
                    nowPlayingList.add(nowPlayingFile)
                    rank++
                }
                if(clear) db?.nowPlaying()!!.delete()
                if(nowPlayingList.size > 0) return (db?.nowPlaying()?.insert(nowPlayingList)!![0] > 0)
                return false
            }
        })
    }

    override fun addNowPlaying(index: Int, file: MediaFile) {
        executorService.submit(Runnable {
            var nowPlayingList  = ArrayList<NowPlayingFile>()
            var nowPlayingFile = NowPlayingFile(file.id, index.toDouble(),false)
            nowPlayingList.add(nowPlayingFile)
            db?.nowPlaying()?.insert(nowPlayingList)
        })

    }

    override fun remove() : LiveData<Int>{
        return QueryExecutor(executorService,object : Callable<Int>{
            override fun call(): Int {
                return db?.library()?.delete()!!
            }
        })
    }

    override fun removeNowPlaying() : LiveData<Int>{
        return QueryExecutor(executorService,object : Callable<Int>{
            override fun call(): Int {
                return db?.nowPlaying()?.delete()!!
            }
        })
    }

    override fun removeNowPlaying(mediaId: Long) {
        executorService.submit(Runnable {
            db?.nowPlaying()?.delete(mediaId)
        })
    }

    override fun remove(mediaId: Long){
        executorService.submit(Runnable {
            db?.library()?.delete(mediaId)
        })
    }

    override fun getMediaFiles(offset: Int, total: Int) : LiveData<List<MediaFile>> {
        return db?.library()!!.get(offset,total)
    }

    override fun setNowPlaying(mediaId: Long) {
        QueryExecutor(executorService,object : Callable<Void>{
            override fun call(): Void? {
                db?.nowPlaying()!!.resetNowPlaying()
                db?.nowPlaying()!!.setNowPlaying(mediaId)
                return null
            }
        })
    }

    override fun getNowPlayList2() : LiveData<List<NowPlayingFile>>{
        return db?.nowPlaying()!!.get()
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

    override fun getMediaFileByAlbum(name: String) : LiveData<List<MediaFile>> {
        return db?.library()!!.getMediaListByAlbum(name)
    }
}