package com.simplesln.simpleplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import com.simplesln.services.MediaPlayerService

abstract class BaseActivity : AppCompatActivity() {
    protected var mediaPlayer: MediaPlayerService? = null
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
            mediaPlayer = binder.getMediaService()
            mBound = true
            onMediaPlayerConnected()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    abstract fun onMediaPlayerConnected()
}