package com.simplesln.simpleplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.simplesln.services.MediaPlayerService

open class BaseActivity : AppCompatActivity() {
    protected var mService: MediaPlayerService? = null
    protected var mBound: Boolean = false

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
            mService = binder.getMediaService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }
}