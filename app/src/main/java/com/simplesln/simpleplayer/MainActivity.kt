package com.simplesln.simpleplayer

import android.content.Intent
import android.os.Bundle
import com.simplesln.data.PrefDataProvider
import com.simplesln.services.MediaScanService

class MainActivity : BaseActivity() {
    private lateinit var pref : PrefDataProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pref = PrefDataProvider(this)
        if(!pref.everIndexed()){
            startService(Intent(applicationContext,MediaScanService::class.java))
        }
    }
}
