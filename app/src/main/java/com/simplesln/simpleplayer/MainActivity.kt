package com.simplesln.simpleplayer

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.*
import android.widget.SeekBar
import com.simplesln.adapters.ViewPagerAdapter
import com.simplesln.data.PrefDataProvider
import com.simplesln.data.RoomDataProvider
import com.simplesln.data.STATE_PLAYING
import com.simplesln.formatDuration
import com.simplesln.getProgress
import com.simplesln.interfaces.MediaPlayer
import com.simplesln.services.MediaPlayerService
import com.simplesln.services.MediaScanService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private lateinit var pref : PrefDataProvider
    private lateinit var dataProvider: RoomDataProvider
    private var countDownTimer: CountDownTimer? = null

    override fun onMediaPlayerConnected() {
        if(mediaPlayer != null){
            mediaPlayer!!.getMediaPlayerState().observe(this, Observer {
                mediaPlayerState ->
                if(mediaPlayerState != null) {
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

                    totalTime.text = formatDuration(mediaPlayer?.duration()!!)
                    currentTime.text = formatDuration(mediaPlayer?.currentPosition()!!)
                    seekBar.progress = getProgress(mediaPlayer?.currentPosition()!!,mediaPlayer?.duration()!!)
                    if(mediaPlayerState.state != STATE_PLAYING){
                        Log.e("Playing","stoping countdown timer")
                        stopCountDownTimer()
                    }
                    else if(mediaPlayerState.state == STATE_PLAYING && countDownTimer == null && mediaPlayer!!.duration() > 0){
                        Log.e("Playing","starting countdown timer")
                        startCountDownTimer(mediaPlayer!!.duration().toLong())
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
        tabLayout.addTab(tabLayout.newTab().setText("Now Playing"))
        tabLayout.addTab(tabLayout.newTab().setText("Albums"))
        tabLayout.addTab(tabLayout.newTab().setText("Artists"))
        tabLayout.addTab(tabLayout.newTab().setText("Genre"))
        tabLayout.addTab(tabLayout.newTab().setText("Playlist"))
        tabLayout.addTab(tabLayout.newTab().setText("Songs"))

        viewPager.adapter = ViewPagerAdapter(6,supportFragmentManager)
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

        pref = PrefDataProvider(this)
        dataProvider = RoomDataProvider(applicationContext)
        if(!pref.everIndexed()){
            startService(Intent(applicationContext,MediaScanService::class.java))
        }

        actionPlay.setOnClickListener(View.OnClickListener {
            if(mediaPlayer != null){
                if(mediaPlayer!!.isPlaying()) {
                    mediaPlayer?.stop()
                }
                else{
                    mediaPlayer?.play()
                }
            }
        })

        actionPrevious.setOnClickListener(View.OnClickListener {
            if(mediaPlayer != null){
                mediaPlayer?.prev()
            }
        })

        actionNext.setOnClickListener(View.OnClickListener {
            if(mediaPlayer != null){
                mediaPlayer?.next()
            }
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(mediaPlayer != null) {
                    val duration = mediaPlayer?.duration()!!
                    currentTime.text = formatDuration((progress * duration / 100))
                }
            }
        })

        seekBar.setOnTouchListener { _ , event ->
            if(event?.action == MotionEvent.ACTION_UP){
                val progress = seekBar.progress
                if(mediaPlayer != null){
                    val duration = mediaPlayer?.duration()!!
                    if(duration > 0){
                        mediaPlayer?.seek((progress * duration / 100))
                    }
                    if(mediaPlayer?.isPlaying()!!) startCountDownTimer(duration.toLong())
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> supportFragmentManager.popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        stopCountDownTimer()
    }

    override fun onResume() {
        super.onResume()
        if(mediaPlayer != null && mediaPlayer!!.isPlaying()) {
            startCountDownTimer(mediaPlayer!!.duration().toLong())
        }
    }

    private fun startCountDownTimer(maxTime : Long){
        countDownTimer = object : CountDownTimer(maxTime,1000){
            override fun onFinish() {
            }

            override fun onTick(millisUntilFinished: Long) {
                if(mediaPlayer != null){
                    val progress = getProgress(mediaPlayer!!.currentPosition(),mediaPlayer!!.duration())
                    if(seekBar.progress == progress){
                        currentTime.text = formatDuration(mediaPlayer!!.currentPosition())
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

    fun getMediaPlayer() : MediaPlayer? {
        return mediaPlayer
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
