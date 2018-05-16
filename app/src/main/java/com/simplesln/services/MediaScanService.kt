package com.simplesln.services

import android.app.Service
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.os.IBinder
import android.util.Base64
import android.util.Log
import com.simplesln.repositories.PrefDataProvider
import com.simplesln.data.entities.MediaFile
import com.simplesln.interfaces.DataProvider
import com.simplesln.simpleplayer.getDataProvider
import com.simplesln.simpleplayer.getPref
import java.io.File

class MediaScanService : Service() {
    private val TAG = "MediaScannerService"
    private lateinit var dataProvider: DataProvider
    private lateinit var pref : PrefDataProvider

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG,"create")
        dataProvider = getDataProvider(this)
        pref = getPref(this)
        pref.scanRunning(true)
        Log.e(TAG,"starting scan")
        MediaScanner()
    }

    override fun onDestroy() {
        super.onDestroy()
        pref.everIndexed(true)
        pref.scanRunning(false)
        Log.e(TAG,"destroyed")
    }

    inner class MediaScanner {
        private val baseFiles = arrayOf(Environment.getExternalStorageDirectory())
        private val patterns = arrayOf(".mp3","aac")

        init {
            scan(baseFiles.toList())
            stopSelf()
        }

        fun scan(files : List<File>){
            val nextDir = ArrayList<File>()
            val filesToSave = ArrayList<MediaFile>()
            for(parent in files){
                for(path in parent.list()) {
                    val file = File(parent.path,path)
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
            val art : String = if(mmr.embeddedPicture != null) Base64.encodeToString(mmr.embeddedPicture,Base64.DEFAULT) else ""

            return MediaFile(file.absolutePath,file.name,duration.toInt(),artist,genre,album,folder,year,art = art)
        }
    }
}