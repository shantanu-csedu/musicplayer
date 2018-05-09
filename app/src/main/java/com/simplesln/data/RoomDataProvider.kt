package com.simplesln.data

import android.arch.lifecycle.LiveData
import android.content.Context
import com.simplesln.data.entities.MediaFile
import com.simplesln.data.entities.NowPlayingFile
import com.simplesln.data.entities.PlayList
import com.simplesln.data.entities.PlayListData
import com.simplesln.interfaces.DataProvider
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class RoomDataProvider(context : Context) : DataProvider{


    private var db : MyDB? = getInstance(context)
    private var executorService : ExecutorService = Executors.newFixedThreadPool(3)

    override fun getNowPlay(): LiveData<MediaFile> {
        return db?.nowPlaying()!!.getNowPlayingItems()
//        val distinctLiveData = MediatorLiveData<MediaFile>()
//        distinctLiveData.addSource(db?.nowPlaying()!!.getNowPlayingItems(), object : Observer<MediaFile> {
//            private var initialized = false
//            private var lastObj: MediaFile? = null
//            override fun onChanged(obj: MediaFile?) {
//                if(obj == null) return
//                if (!initialized) {
//                    initialized = true
//                    lastObj = obj
//                    distinctLiveData.postValue(lastObj)
//                } else if (obj != lastObj) {
//                    lastObj = obj
//                    distinctLiveData.postValue(lastObj)
//                }
//            }
//        })
//        return distinctLiveData
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
        return QueryExecutor(executorService, Callable<Boolean> {
            val nowPlayingList  = ArrayList<NowPlayingFile>()
            var rank = 0.0
            if(clear) {
                db?.nowPlaying()!!.delete()
            }
            else{
                rank = db?.nowPlaying()!!.getMaxRank() + 1
            }
            for((index,file) in files.withIndex()){
                val nowPlayingFile = NowPlayingFile(file.id,rank+index,false)
                nowPlayingList.add(nowPlayingFile)
//                Log.e("add nowplaying","" + (rank+index) + " " + file.name)
            }

            if(nowPlayingList.size > 0) return@Callable (db?.nowPlaying()?.insert(nowPlayingList)!![0] > 0)
            false
        })
    }

    override fun addNowPlaying(index: Int, file: MediaFile) {
        executorService.submit({
            val nowPlayingList  = ArrayList<NowPlayingFile>()
            val nowPlayingFile = NowPlayingFile(file.id, index.toDouble(),false)
            nowPlayingList.add(nowPlayingFile)
            db?.nowPlaying()?.insert(nowPlayingList)
        })

    }

    override fun remove() : LiveData<Int>{
        return QueryExecutor(executorService, Callable<Int> { db?.library()?.delete()!! })
    }

    override fun removeNowPlaying() : LiveData<Int>{
        return QueryExecutor(executorService,object : Callable<Int>{
            override fun call(): Int {
                return db?.nowPlaying()?.delete()!!
            }
        })
    }

    override fun removeNowPlaying(mediaId: Long) {
        executorService.submit({
            val npId = db?.nowPlaying()?.getNowPlayingId()
            if(mediaId == npId){//deleted item is currently playing, need to set another now playing
                var nextFile = db?.nowPlaying()?.getNextSync()
                if(nextFile == null){
                    nextFile = db?.nowPlaying()?.getPreviousSync()
                }
                if(nextFile != null){//only 1 file
                    db?.nowPlaying()?.setNowPlaying(nextFile.id)
                }
            }
            db?.nowPlaying()?.delete(mediaId)
        })
    }

    override fun remove(mediaId: Long){
        executorService.submit({
            db?.library()?.delete(mediaId)
        })
    }

    override fun getMediaFiles(offset: Int, total: Int) : LiveData<List<MediaFile>> {
        return db?.library()!!.get(offset,total)
    }

    override fun setNowPlaying(mediaId: Long) {
        QueryExecutor(executorService, Callable<Void> {
            db?.nowPlaying()!!.resetNowPlaying()
            db?.nowPlaying()!!.setNowPlaying(mediaId)
            null
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
            db?.nowPlaying()!!.getAvgRank(fromId,toId)
        })
    }

    override fun getRank(id: Long, before: Boolean): LiveData<Double> {
        return QueryExecutor(executorService, Callable<Double>{
            if(before) db?.nowPlaying()!!.getRank(id) -1
            else db?.nowPlaying()!!.getRank(id) + 1
        })
    }

    override fun updateRank(file: MediaFile, rank: Double) {
        QueryExecutor(executorService, Callable<Void> {
            val npid = db?.nowPlaying()?.getNowPlayingId()
            val nowPlayingFile = NowPlayingFile(file.id, rank, file.id == npid)
            nowPlayingFile.id = db?.nowPlaying()?.getNowPlayingItem(file.id)!!
            if(nowPlayingFile.id > 0) {
                db?.nowPlaying()?.update(Arrays.asList(nowPlayingFile))
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
        return QueryExecutor(executorService,Callable<Long>{
            val playList = PlayList(name)
             db?.playlist()?.insert(playList)
        })
    }

    override fun addToPlayList(playListName: String, mediaFiles: List<MediaFile>) {
        QueryExecutor(executorService,Callable<Void>{
            val playlistDataList = ArrayList<PlayListData>()
            var playlistId = db?.playlist()?.getPlaylistId(playListName)
            if(playlistId == -1L){//doesn't exits
                playlistId = db?.playlist()?.insert(PlayList(playListName))
            }
            for(mediaFile in mediaFiles){
                val playlistData = PlayListData(mediaFile.id, playlistId!!)
                playlistDataList.add(playlistData)
            }
            db?.playlist()?.insertPlayListData(playlistDataList)
            null
        })
    }

    override fun setNext(id: Long) {
        QueryExecutor(executorService,Callable<Void>{
            val nowPlayingItem = db?.nowPlaying()?.getNowPlayingItemSync()
            val nextPlayingItem = db?.nowPlaying()?.getNextSync()
            val mediaFile = db?.nowPlaying()?.get(id)
            if(mediaFile == null){ //insert
                if(nowPlayingItem == null){
                    db?.nowPlaying()?.insert(Arrays.asList(NowPlayingFile(id,0.0,true)))
                }
                else if(nextPlayingItem == null){ //no next item
                    db?.nowPlaying()?.insert(Arrays.asList(NowPlayingFile(id,db?.nowPlaying()?.getRank(nowPlayingItem.id)!! + 1,false)))
                }
                else{
                    db?.nowPlaying()?.insert(Arrays.asList(NowPlayingFile(id,db?.nowPlaying()?.getAvgRank(nowPlayingItem.id,nextPlayingItem.id)!!,false)))
                }
            }
            else{ //update
                if(nowPlayingItem == null){
                    mediaFile.rank = 0.0
                    mediaFile.nowPlaying = true
                    db?.nowPlaying()?.update(Arrays.asList(mediaFile))
                }
                else if(nextPlayingItem == null){ //no next item
                    mediaFile.rank = db?.nowPlaying()?.getRank(nowPlayingItem.id)!! + 1
                    mediaFile.nowPlaying = false
                    db?.nowPlaying()?.update(Arrays.asList(mediaFile))
                }
                else{
                    mediaFile.rank = db?.nowPlaying()?.getAvgRank(nowPlayingItem.id,nextPlayingItem.id)!!
                    mediaFile.nowPlaying = false
                    db?.nowPlaying()?.update(Arrays.asList(mediaFile))
                }
            }
            null
        })
    }
}