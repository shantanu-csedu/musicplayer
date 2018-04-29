package com.simplesln.fragments

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.simplesln.adapters.NowPlayListAdapter
import com.simplesln.data.MediaPlayerState
import com.simplesln.data.STATE_PLAYING
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R
import kotlinx.android.synthetic.main.fragment_now_playing.*

class NowPlayingFragment : Fragment(), AdapterView.OnItemClickListener {
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val mediaFile = mAdapter.values[position]
        (activity as MainActivity).getDataProvider().setNowPlaying(mediaFile.id)
    }

    lateinit var listView : RecyclerView
    lateinit var mAdapter : NowPlayListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = NowPlayListAdapter(activity!!)
        mAdapter.setOnItemClickListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_now_playing,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        listView.layoutManager = LinearLayoutManager(activity)
        listView.adapter = mAdapter

        observeNowPlaying()
//        val mediaPlayerService = (activity as MainActivity).mediaPlayerService
//        mediaPlayerService?.getMediaPlayerState()?.observe(this, Observer {
//            if(it?.state == STATE_PLAYING){
//                mAdapter.setCurrentMediaFile(it.mediaFile)
//                mAdapter.notifyDataSetChanged()
//            } else{
//                mAdapter.setCurrentMediaFile(null)
//                mAdapter.notifyDataSetChanged()
//            }
//        })
    }

    private fun observeNowPlaying(){
        (activity as MainActivity).getDataProvider().getNowPlayList().observe(this, Observer {
            mAdapter.values.clear()
            if (it != null) {
                mAdapter.values.addAll(it)
            }
            mAdapter.notifyDataSetChanged()
        })
    }

}