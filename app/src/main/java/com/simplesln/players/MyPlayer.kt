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