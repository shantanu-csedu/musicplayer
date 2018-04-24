package com.simplesln.services

import android.app.Service
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.os.IBinder
import android.util.Log
import com.simplesln.data.PrefDataProvider
import com.simplesln.data.RoomDataProvider
import com.simplesln.data.entities.MediaFile
import java.io.File

class MediaScanService : Service() {
    private val TAG = "MediaScannerService"
    private lateinit var dataProvider: RoomDataProvider
    private lateinit var pref : PrefDataProvider

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG,"create")
        dataProvider = RoomDataProvider(this)
        pref = PrefDataProvider(this)
        pref.scanRunning(true)
        Log.e(TAG,"starting scan")
        Mp3Scanner()
    }

    override fun onDestroy() {
        super.onDestroy()
        pref.everIndexed(true)
        pref.scanRunning(false)
        Log.e(TAG,"destroyed")
    }

    inner class Mp3Scanner {
        private val baseFiles = arrayOf(Environment.getExternalStorageDirectory())
        private val patterns = arrayOf(".mp3")

        init {
            scan(baseFiles.toList())
            stopSelf()
        }

        fun scan(files : List<File>){
            var nextDir = ArrayList<File>()
            var filesToSave = ArrayList<MediaFile>()
            for(parent in files){
                for(path in parent.list()) {
                    var file = File(parent.path,path)
                    if (file.isDirectory && !file.name.startsWith(".") && !file.name.startsWith("Android")) nextDir.add(file)
                    else if (!file.isDirectory && isMatchPattern(file.name)) {
                        filesToSave.add(getMediaFile(file))
                    }
                }
            }
            if(filesToSave.size > 0 ) dataProvider.addMedia(filesToSave)
            if(nextDir.size > 0){
                Log.e("base path ", nextDir[0].path)
                scan(nextDir)
            }
        }

        private fun isMatchPattern(name : String) : Boolean{
            for(pattern in patterns){
                if(name.endsWith(pattern,false)){
                    return true
                }
            }
            return false
        }

        private fun getMediaFile(file : File) : MediaFile{
            val folder = file.parent.substring(file.parent.lastIndexOf("/")+1)
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(file.absolutePath);
            val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val genre =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            val year = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

            return MediaFile(file.absolutePath,file.name,duration.toInt(),artist,genre,album,folder,year)
        }
    }
}