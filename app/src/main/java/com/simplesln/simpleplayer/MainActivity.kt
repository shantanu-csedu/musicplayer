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

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.simplesln.adapters.ViewPagerAdapter
import com.simplesln.appVersion
import com.simplesln.data.*
import com.simplesln.formatDuration
import com.simplesln.fragments.TitleFragment
import com.simplesln.getProgress
import com.simplesln.interfaces.DataProvider
import com.simplesln.repositories.PrefDataProvider
import com.simplesln.services.MediaScanService
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.analytics.HitBuilders



const val NOW_PLAYING = "Now Playing"
const val PLAYLIST = "Playlist"
const val ALBUM = "Album"
const val ARTIST = "Artist"
const val GENRE = "Genre"
const val SONGS = "Songs"
const val LIBRARY = "Library"
class MainActivity : BaseActivity() {

    private lateinit var pref : PrefDataProvider
    private lateinit var dataProvider: DataProvider
    private var countDownTimer: CountDownTimer? = null
    val TABS = arrayOf(NOW_PLAYING, LIBRARY)//, ALBUM, ARTIST, PLAYLIST, GENRE, SONGS)
    val liveMediaPlayerState : LiveMediaPlayerState = LiveMediaPlayerState()

    override fun onMediaPlayerConnected() {
        if(mediaPlayerService != null){
            liveMediaPlayerState.update(mediaPlayerService!!.getMediaPlayerState().lastState)
            mediaPlayerService!!.getMediaPlayerState().observe(this, Observer {
                mediaPlayerState ->
                if(mediaPlayerState != null) {
                    liveMediaPlayerState.update(mediaPlayerState)
                    val mediaFile = mediaPlayerState.mediaFile
                    if(mediaFile != null){
                        if(playerControlContainer.visibility == View.GONE) playerControlContainer.visibility = View.VISIBLE
                        songTitle.text = mediaFile.name
                        artistName.text = mediaFile.artist
                    }
                    else{
                        if(playerControlContainer.visibility == View.VISIBLE) playerControlContainer.visibility = View.GONE
                        songTitle.text = ""
                        artistName.text = ""
                    }

                    if (mediaPlayerState.state == STATE_PLAYING) {
                        actionPlay.setImageResource(R.mipmap.ic_pause)
                    } else {
                        actionPlay.setImageResource(R.mipmap.ic_play)
                    }

                    totalTime.text = formatDuration(mediaPlayerService?.getPlayer()!!.duration())
                    currentTime.text = formatDuration(mediaPlayerService?.getPlayer()!!.currentPosition())
                    seekBar.progress = getProgress(mediaPlayerService?.getPlayer()!!.currentPosition(),mediaPlayerService?.getPlayer()!!.duration())
                    if(mediaPlayerState.state != STATE_PLAYING){
                        Log.e("Playing","stoping countdown timer")
                        stopCountDownTimer()
                        songTitle.setHorizontallyScrolling(false);
                        songTitle.isSelected = false;
                    }
                    else if(mediaPlayerState.state == STATE_PLAYING && countDownTimer == null && mediaPlayerService?.getPlayer()!!.duration() > 0){
                        Log.e("Playing","starting countdown timer")
                        startCountDownTimer(mediaPlayerService?.getPlayer()!!.duration().toLong())
                        songTitle.setHorizontallyScrolling(true);
                        songTitle.isSelected = true;
                    }
                    Log.e("Playing state","" + mediaPlayerState.state)
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        pref = getPref(this)
        dataProvider = getDataProvider(this)
        if(!pref.everIndexed() && askStorageWritePermission()){
            startService(Intent(applicationContext,MediaScanService::class.java))
        }
        actionPlay.setOnClickListener(View.OnClickListener {
            if(mediaPlayerService != null){
                if(mediaPlayerService?.getPlayer()!!.isPlaying()) {
                    mediaPlayerService?.getPlayer()!!.stop()
                }
                else{
                    mediaPlayerService?.getPlayer()!!.play()
                }
            }
        })

        actionPrevious.setOnClickListener(View.OnClickListener {
            if(mediaPlayerService != null){
                mediaPlayerService?.getPlayer()!!.prev()
            }
        })

        actionNext.setOnClickListener(View.OnClickListener {
            if(mediaPlayerService != null){
                mediaPlayerService?.getPlayer()!!.next()
            }
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(mediaPlayerService != null) {
                    val duration = mediaPlayerService?.getPlayer()!!.duration()
                    currentTime.text = formatDuration((progress * duration / 100))
                }
            }
        })

        seekBar.setOnTouchListener { _ , event ->
            if(event?.action == MotionEvent.ACTION_UP){
                val progress = seekBar.progress
                if(mediaPlayerService != null){
                    val duration = mediaPlayerService?.getPlayer()!!.duration()
                    if(duration > 0){
                        mediaPlayerService?.getPlayer()!!.seek(Math.ceil(progress * duration / 100.0).toInt())
                    }
                    if(mediaPlayerService?.getPlayer()!!.isPlaying()) startCountDownTimer(duration.toLong())
                }
            } else if(event?.action == MotionEvent.ACTION_DOWN){
                stopCountDownTimer()
            }
            Log.e("Touch","Action " + event?.action)
            false
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 0){
                title = ""
                tabLayout.visibility = View.VISIBLE
                viewPager.visibility = View.VISIBLE
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
            else if(supportFragmentManager.findFragmentById(R.id.songListContainer) is TitleFragment){
                tabLayout.visibility = View.GONE
                viewPager.visibility = View.GONE
                title = (supportFragmentManager.findFragmentById(R.id.songListContainer) as TitleFragment).getTitle()
            }
        }

        playerRowContainer.setOnClickListener {
            while(supportFragmentManager.backStackEntryCount > 0){
                supportFragmentManager.popBackStackImmediate()
            }
            viewPager.setCurrentItem(0,true)
        }

        for(tab in TABS){
            tabLayout.addTab(tabLayout.newTab().setText(tab))
        }
        viewPager.adapter = ViewPagerAdapter(TABS,supportFragmentManager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                tabLayout.getTabAt(position)?.select()
            }

        })

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab != null)
                    viewPager.setCurrentItem(tab.position,true)
            }

        })

        if(!pref.everIndexed()){
            tabLayout.getTabAt(1)?.select()
        }

        try {
            //sending analytics
            getDefaultTracker(this).send(HitBuilders.EventBuilder()
                    .setCategory("Screen")
                    .setAction("Open")
                    .setLabel("Main activity")
                    .build())
        }catch (ex : Exception){
            ex.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> supportFragmentManager.popBackStack()
            R.id.menu_scan -> {
                startService(Intent(this,MediaScanService::class.java))
                Log.e("scan","started");
                Snackbar.make(viewPager,"Scanning library...",Snackbar.LENGTH_SHORT).show()
            }
            R.id.menu_about ->{
                val builder = AlertDialog.Builder(this@MainActivity)
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val v = inflater.inflate(R.layout.dialog_about, null)
                val text = v.findViewById(R.id.about_version) as TextView
                text.text = resources.getString(R.string.version) + " " + appVersion(this) + "\n"
                text.append(resources.getString(R.string.copyright))
                builder.setView(v)
                builder.create().show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        stopCountDownTimer()
    }

    override fun onResume() {
        super.onResume()
        if(mediaPlayerService != null && mediaPlayerService?.getPlayer()!!.isPlaying()) {
            startCountDownTimer(mediaPlayerService?.getPlayer()!!.duration().toLong())
        }
    }

    private fun startCountDownTimer(maxTime : Long){
        countDownTimer = object : CountDownTimer(maxTime,1000){
            override fun onFinish() {
            }

            override fun onTick(millisUntilFinished: Long) {
                if(mediaPlayerService != null){
                    val progress = getProgress(mediaPlayerService?.getPlayer()!!.currentPosition(),mediaPlayerService?.getPlayer()!!.duration())
                    if(seekBar.progress == progress){
                        currentTime.text = formatDuration(mediaPlayerService?.getPlayer()!!.currentPosition())
                    }
                    else{
                        seekBar.progress = progress
                    }
                }
            }
        }.start()
    }

    private fun stopCountDownTimer(){
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun addLibraryDetailsFragment(fragment: TitleFragment) {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction()
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.songListContainer,fragment)
                .addToBackStack("")
                .commit()
    }

    private fun askStorageWritePermission() : Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQ_READ_STORAGE);
            return false;
        }
        return true
    }
    private val REQ_READ_STORAGE = 2;
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_READ_STORAGE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //success
                if(!pref.everIndexed()){
                    startService(Intent(applicationContext,MediaScanService::class.java))
                }
            }
            else{
                Snackbar.make(viewPager,"Storage permission is required to load music",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Allow", View.OnClickListener {
                            askStorageWritePermission()
                        })
                        .show()
            }
        }
    }
}
