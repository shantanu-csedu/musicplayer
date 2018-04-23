package com.simplesln.simpleplayer

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import com.simplesln.data.PrefDataProvider
import com.simplesln.data.RoomDataProvider
import com.simplesln.data.STATE_PLAYING
import com.simplesln.data.entities.MediaFile
import com.simplesln.services.MediaScanService
import kotlinx.android.synthetic.main.activity_main.*
import java.sql.Time
import java.text.DecimalFormat
import java.util.*
import kotlin.math.max

class MainActivity : BaseActivity() {
    override fun onMediaPlayerConnected() {
        if(mediaPlayer != null){
            mediaPlayer!!.getMediaPlayerState().observe(this, Observer {
                mediaPlayerState ->
                if(mediaPlayerState != null) {
                    var mediaFile = mediaPlayerState.mediaFile
                    if(mediaFile != null){
                        if(playerControlContainer.visibility == View.GONE) playerControlContainer.visibility = View.VISIBLE
                        songTitle.setText(mediaFile.name)
                        artistName.setText(if(mediaFile.artist.length == 0) "unknown" else mediaFile.artist)
                    }
                    else{
                        if(playerControlContainer.visibility == View.VISIBLE) playerControlContainer.visibility = View.GONE
                        songTitle.setText("")
                        artistName.setText("")
                    }

                    if (mediaPlayerState.state == STATE_PLAYING) {
                        actionPlay.setImageResource(R.mipmap.ic_pause)
                    } else {
                        actionPlay.setImageResource(R.mipmap.ic_play)
                    }

                    totalTime.setText(formatDuration(mediaPlayer?.duration()!!))
                    currentTime.setText(formatDuration(mediaPlayer?.currentPosition()!!))
                    seekBar.progress = getProgress(mediaPlayer?.currentPosition()!!,mediaPlayer?.duration()!!)
                    if(mediaPlayerState.state != STATE_PLAYING){
                        stopCountDownTimer()
                    }
                    else if(mediaPlayerState.state == STATE_PLAYING && countDownTimer == null){
                        startCountDownTimer(mediaPlayer!!.duration().toLong())
                    }
                }
            })
        }
    }

    private lateinit var pref : PrefDataProvider
    private lateinit var dataProvider: RoomDataProvider
    private var timer : Timer? = null
    private var countDownTimer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(mediaPlayer != null) {
                    var duration = mediaPlayer?.duration()!!
                    currentTime.setText(formatDuration((progress * duration / 100)))
                }
            }
        })

        seekBar.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if(event?.action == MotionEvent.ACTION_UP){
                    var progress = seekBar.progress
                    if(mediaPlayer != null){
                        var duration = mediaPlayer?.duration()!!
                        if(duration > 0){
                            mediaPlayer?.seek((progress * duration / 100))
                        }
                        if(mediaPlayer?.isPlaying()!!) startCountDownTimer(duration.toLong())
                    }
                }
                else if(event?.action == MotionEvent.ACTION_DOWN){
                    stopCountDownTimer()
                }
                Log.e("Touch","Action " + event?.action)
                return false
            }
        })

    }

    override fun onPause() {
        super.onPause()
//        timer?.cancel()
//        timer?.purge()
//        timer = null
        stopCountDownTimer()
    }

    override fun onResume() {
        super.onResume()
//        timer = Timer()
//        timer?.scheduleAtFixedRate(object : TimerTask(){
//            override fun run() {
//                if(mediaPlayer != null) seekBar.setProgress(getProgress(mediaPlayer!!.currentPosition(),mediaPlayer!!.duration()))
//            }
//
//            fun getProgress(current : Int, duration : Int) : Int {
//                if(duration == 0) return  0
//                return current * 100 / duration
//            }
//        },0,1000)
        if(mediaPlayer != null && mediaPlayer!!.isPlaying()) {
            startCountDownTimer(mediaPlayer!!.duration().toLong())
        }
    }

    private fun startCountDownTimer(maxTime : Long){
        countDownTimer = object : CountDownTimer(maxTime,1000){
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
//                Log.e("Tick","ontick")
                if(mediaPlayer != null){
                    var progress = getProgress(mediaPlayer!!.currentPosition(),mediaPlayer!!.duration())
                    if(seekBar.progress == progress){
                        currentTime.setText(formatDuration(mediaPlayer!!.currentPosition()))
                    }
                    else{
                        seekBar.setProgress(progress)
                    }
                }
            }
        }
        countDownTimer?.start()
    }

    private fun stopCountDownTimer(){
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun getProgress(current : Int, duration : Int) : Int {
        if(duration == 0) return  0
        return current * 100 / duration
    }

    private fun formatDuration(duration : Int) : String{
        var fstring = ""
        var decimalFormat = DecimalFormat("#00")
        var sec =(duration / 1000)
        if( (sec / 3600) > 0) {
            fstring += decimalFormat.format(sec / 3600) + ":";
            sec %= 3600
        }
        fstring += decimalFormat.format(sec/60)
        sec %= 60
        fstring += ":" + decimalFormat.format(sec)
        return fstring
    }
}
