package com.simplesln.simpleplayer

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.simplesln.adapters.ViewPagerAdapter
import com.simplesln.data.*
import com.simplesln.formatDuration
import com.simplesln.getProgress
import com.simplesln.services.MediaScanService
import kotlinx.android.synthetic.main.activity_main.*

const val NOW_PLAYING = "Now Playing"
const val PLAYLIST = "Playlist"
const val ALBUM = "Album"
const val ARTIST = "Artist"
const val GENRE = "Genre"
const val SONGS = "Songs"
class MainActivity : BaseActivity() {

    private lateinit var pref : PrefDataProvider
    private lateinit var dataProvider: RoomDataProvider
    private var countDownTimer: CountDownTimer? = null
    val TABS = arrayOf(NOW_PLAYING, PLAYLIST, ALBUM, ARTIST, GENRE, SONGS)
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
                    }
                    else if(mediaPlayerState.state == STATE_PLAYING && countDownTimer == null && mediaPlayerService?.getPlayer()!!.duration() > 0){
                        Log.e("Playing","starting countdown timer")
                        startCountDownTimer(mediaPlayerService?.getPlayer()!!.duration().toLong())
                    }
                    Log.e("Playing state","" + mediaPlayerState.state)
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        pref = PrefDataProvider(this)
        dataProvider = RoomDataProvider(applicationContext)
        if(!pref.everIndexed()){
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
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
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

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> supportFragmentManager.popBackStack()
            R.id.menu_scan -> {
                startService(Intent(applicationContext,MediaScanService::class.java))
                Toast.makeText(this,"Scanning library...",Toast.LENGTH_SHORT).show()
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

    fun getDataProvider() : RoomDataProvider{
        return dataProvider
    }

    fun addDetailsFragment(title: String, fragment: Fragment) {
        this.title = title
        tabLayout.visibility = View.GONE
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction()
                .replace(R.id.songListContainer,fragment)
                .addToBackStack("")
                .commit()
    }
}
