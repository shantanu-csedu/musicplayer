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

package com.simplesln.services

import android.app.Service
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.Observer
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.util.Base64
import android.util.Log
import com.simplesln.repositories.PrefDataProvider
import com.simplesln.data.entities.MediaFile
import com.simplesln.interfaces.DataProvider
import com.simplesln.simpleplayer.getDataProvider
import com.simplesln.simpleplayer.getPref
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class MediaScanService : LifecycleService() {
    private val TAG = "MediaScannerService"
    private lateinit var dataProvider: DataProvider
    private lateinit var pref : PrefDataProvider
    private val executorService : ExecutorService = Executors.newFixedThreadPool(1)


    override fun onCreate() {
        super.onCreate()
        Log.e(TAG,"create")
        dataProvider = getDataProvider(this)
        pref = getPref(this)
        pref.scanRunning(true)
        executorService.submit(MediaScanner())
        Log.e(TAG,"starting scan")
    }

    override fun onDestroy() {
        super.onDestroy()
        pref.everIndexed(true)
        pref.scanRunning(false)
        executorService.shutdown()
        Log.e(TAG,"destroyed")
    }

    inner class MediaScanner : Runnable {
        override fun run() {
            deleteInvalid()
            scan(getBaseFiles(ContextCompat.getExternalFilesDirs(this@MediaScanService, null)))
            stopSelf()
        }

//        private val baseFiles = ContextCompat.getExternalFilesDirs(this@MediaScanService, null)
        private val patterns = arrayOf(".mp3","aac","flac")

        private fun getBaseFiles(files : Array<File>) : List<File>{
            val suffix = "/Android/data/com.simplesln.simpleplayer"
            val baseFiles = ArrayList<File>()
            for(file in files){
                baseFiles.add(File(file.absolutePath.substring(0,file.absolutePath.indexOf(suffix))))
            }
            return baseFiles
        }
        private fun scan(files : List<File>){
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

        private fun deleteInvalid(){
            val liveData = dataProvider.getMediaFiles()
            liveData.observe(this@MediaScanService ,object :Observer<List<MediaFile>>{
                override fun onChanged(it: List<MediaFile>?) {
                    liveData.removeObserver(this)
                    if(it != null) {
                        for (file in it) {
                            if(!File(file.link).exists()){
                                dataProvider.removeMedia(file.id)
                            }
                        }
                    }
                }
            })
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