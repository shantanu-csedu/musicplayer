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

package com.simplesln.simpleplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.simplesln.services.MediaPlayerService

abstract class BaseActivity : AppCompatActivity() {
    var mediaPlayerService: MediaPlayerService? = null
    protected var mBound: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(applicationContext,MediaPlayerService::class.java))
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, MediaPlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE);
    }

    override fun onStop() {
        super.onStop()
        unbindService(mConnection)
        mBound = false
    }

    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val binder = service as MediaPlayerService.MediaPlayerServiceBinder
            mediaPlayerService = binder.getMediaService()
            mBound = true
            onMediaPlayerConnected()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    abstract fun onMediaPlayerConnected()
}