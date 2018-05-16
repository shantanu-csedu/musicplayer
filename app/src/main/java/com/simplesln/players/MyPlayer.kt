package com.simplesln.players

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import com.simplesln.data.entities.MediaFile
import com.simplesln.interfaces.DataProvider
import com.simplesln.interfaces.Player

abstract class MyPlayer(private val lifecycleOwner: LifecycleOwner, val dataProvider: DataProvider) : Player {
    override fun next() {
        val nextLiveData = dataProvider.getNext();
        val nextDataObserver = object : Observer<MediaFile> {
            override fun onChanged(mediaFile: MediaFile?) {
                nextLiveData.removeObserver(this)
                if(mediaFile != null) {
                    dataProvider.setNowPlaying(mediaFile.id)
                }
            }
        }
        nextLiveData.observe(lifecycleOwner,nextDataObserver)
    }

    override fun prev() {
        val prevLiveData = dataProvider.getPrev();
        val prevDataObserver = object : Observer<MediaFile> {
            override fun onChanged(mediaFile: MediaFile?) {
                prevLiveData.removeObserver(this)
                if(mediaFile != null) {
                    dataProvider.setNowPlaying(mediaFile.id)
                }
            }
        }
        prevLiveData.observe(lifecycleOwner,prevDataObserver)
    }
}