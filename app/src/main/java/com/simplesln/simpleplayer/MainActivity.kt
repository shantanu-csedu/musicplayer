package com.simplesln.simpleplayer

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.simplesln.data.PrefDataProvider
import com.simplesln.data.RoomDataProvider
import com.simplesln.data.entities.MediaFile
import com.simplesln.services.MediaScanService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    override fun onMediaPlayerInitialized() {
//        var mediaFileObserver = Observer<List<MediaFile>> {
//            list ->
//            if(list != null){
//                var nowPlaying  = ArrayList<MediaFile>()
//                var nowPlayId = 0L;
//                for(mediaFile in list){
//                    Log.e("Name",mediaFile.name);
//                    Log.e("Path",mediaFile.link);
//                    Log.e("Folder",mediaFile.folder)
//                    if(mediaFile.folder.equals("Music")){
//                        if(nowPlayId == 0L) nowPlayId = mediaFile.id
//                        nowPlaying.add(mediaFile)
//                    }
//                }
//                if(nowPlaying.size > 0) {
//                    var removeNowPlayingObserver = Observer<Int> {
//                        dataProvider.addNowPlaying(nowPlaying, nowPlayId).observe(this, Observer {
//                            result ->
//                        })
//                    }
//                    dataProvider.removeNowPlaying().observe(this, removeNowPlayingObserver)
//                }
//            }
//        }
//        dataProvider.getMediaFiles(0,10).observe(this,mediaFileObserver)
    }

    private lateinit var pref : PrefDataProvider
    private lateinit var dataProvider: RoomDataProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pref = PrefDataProvider(this)
        dataProvider = RoomDataProvider(applicationContext)
        if(!pref.everIndexed()){
            startService(Intent(applicationContext,MediaScanService::class.java))
        }

        button1.setOnClickListener(View.OnClickListener {
            if(mediaPlayer != null){
                if(mediaPlayer!!.isPlaying()) {
                    mediaPlayer?.stop()
                }
                else{
                    mediaPlayer?.play()
                }
            }
        })

        button2.setOnClickListener(View.OnClickListener {
            if(mediaPlayer != null){
                mediaPlayer?.prev()
            }
        })

        button3.setOnClickListener(View.OnClickListener {
            if(mediaPlayer != null){
                mediaPlayer?.next()
            }
        })

        button0.setOnClickListener(View.OnClickListener {
            var mediaFileLiveData = dataProvider.getMediaFiles(0,10)
            var mediaFileObserver = object  : Observer<List<MediaFile>> {
                override fun onChanged(list : List<MediaFile>?) {
                    if(list != null){
                        mediaFileLiveData.removeObserver(this)
                        var nowPlaying  = ArrayList<MediaFile>()
                        var nowPlayId = 0L;
                        for(mediaFile in list){
                            Log.e("Name",mediaFile.name);
                            Log.e("Path",mediaFile.link);
                            Log.e("Folder",mediaFile.folder)
                            if(mediaFile.folder.equals("Music")){
                                if(nowPlayId == 0L) nowPlayId = mediaFile.id
                                nowPlaying.add(mediaFile)
                            }
                        }
                        if(nowPlaying.size > 0) {
                            var nowPlayingLiveData = dataProvider.addNowPlaying(nowPlaying,true)
                            nowPlayingLiveData.observe(this@MainActivity,object : Observer<Boolean>{
                                override fun onChanged(result: Boolean?) {
                                    nowPlayingLiveData.removeObserver(this)
                                    dataProvider.setNowPlaying(nowPlayId)
                                }
                            })
                        }
                    }
                }
            }
            mediaFileLiveData.observe(this,mediaFileObserver)
        })
    }
}
