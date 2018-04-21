package com.simplesln.data

import android.arch.lifecycle.LiveData
import android.content.Context
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.NowPlayingFile
import com.simplesln.interfaces.DataProvider
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RoomDataProvider(context : Context) : DataProvider{

    private var db : MyDB?
    private var executorService : ExecutorService
    init {
        db = getInstance(context)
        executorService = Executors.newFixedThreadPool(3)
    }

    override fun getNowPlay(): LiveData<MediaFile> {
        return db?.nowPlaying()!!.getNowPlayingItem()
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
        executorService.submit(Runnable {
            db?.library()?.insert(files)
        })
    }

    override fun addNowPlaying(files: List<MediaFile>, nowPlayingId : Long) {
        executorService.submit(Runnable {
            var nowPlayingList  = ArrayList<NowPlayingFile>()
            for((rank, file) in files.withIndex()){
                var nowPlayingFile = NowPlayingFile(file.id,rank,(file.id == nowPlayingId))
                nowPlayingList.add(nowPlayingFile)
            }
            db?.nowPlaying()?.insert(nowPlayingList)
        })
    }

    override fun addNowPlaying(index: Int, file: MediaFile) {
        executorService.submit(Runnable {
            var nowPlayingList  = ArrayList<NowPlayingFile>()
            var nowPlayingFile = NowPlayingFile(file.id,index,false)
            nowPlayingList.add(nowPlayingFile)
            db?.nowPlaying()?.insert(nowPlayingList)
        })

    }

    override fun remove() {
        executorService.submit(Runnable {
            db?.library()?.delete()
        })
    }

    override fun removeNowPlaying() {
        executorService.submit(Runnable {
            db?.nowPlaying()?.delete()
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
}