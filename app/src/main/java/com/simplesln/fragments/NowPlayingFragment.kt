package com.simplesln.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.simplesln.adapters.NowPlayListAdapter
import com.simplesln.adapters.helper.ItemTouchHelperAdapter
import com.simplesln.adapters.helper.SimpleItemTouchHelperCallback
import com.simplesln.data.MediaPlayerState
import com.simplesln.data.STATE_PLAYING
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R
import kotlinx.android.synthetic.main.fragment_now_playing.*

class NowPlayingFragment : Fragment(), AdapterView.OnItemClickListener, ItemTouchHelperAdapter {
    override fun onItemReleased() {
        val toPosition = mAdapter.moveToIndex
        mAdapter.moveToIndex = -1
        if(toPosition >= 0){
            val topPosition = toPosition - 1
            val bottomPosition = toPosition + 1
            if(topPosition >= 0 && bottomPosition < mAdapter.values.size){
                val rankLiveData = (activity as MainActivity).getDataProvider().getRank(mAdapter.values[topPosition].id,mAdapter.values[bottomPosition].id)
                rankLiveData.observe(this,object : Observer<Double>{
                    override fun onChanged(rank: Double?) {
                        rankLiveData.removeObserver(this)
                        (activity as MainActivity).getDataProvider().updateRank(mAdapter.values[toPosition],rank!!)
                    }
                })
            }
            else if(topPosition < 0 && bottomPosition < mAdapter.values.size){
                Log.e("bottom",mAdapter.values[bottomPosition].name)
                val rankLiveData = (activity as MainActivity).getDataProvider().getRank(mAdapter.values[bottomPosition].id,true)
                rankLiveData.observe(this,object : Observer<Double>{
                    override fun onChanged(rank: Double?) {
                        rankLiveData.removeObserver(this)
                        (activity as MainActivity).getDataProvider().updateRank(mAdapter.values[toPosition],rank!!)
                    }
                })
            }
            else if(topPosition >=0 && bottomPosition >= mAdapter.values.size){
                val rankLiveData = (activity as MainActivity).getDataProvider().getRank(mAdapter.values[topPosition].id,false)
                rankLiveData.observe(this,object : Observer<Double>{
                    override fun onChanged(rank: Double?) {
                        rankLiveData.removeObserver(this)
                        (activity as MainActivity).getDataProvider().updateRank(mAdapter.values[toPosition],rank!!)
                    }
                })
            }
            //else single item
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        mAdapter.onItemMove(fromPosition,toPosition)
    }

    override fun onItemDismiss(position: Int) {
        (activity as MainActivity).getDataProvider().removeNowPlaying(mAdapter.values[position].id)
        mAdapter.onItemDismiss(position)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val mediaFile = mAdapter.values[position]
        (activity as MainActivity).getDataProvider().setNowPlaying(mediaFile.id)
    }

    lateinit var listView : RecyclerView
    lateinit var mAdapter : NowPlayListAdapter
    lateinit var mImageTouchHelper : ItemTouchHelper
    private val handler = Handler()

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

        val callback = SimpleItemTouchHelperCallback(this)
        mImageTouchHelper = ItemTouchHelper(callback)
        mImageTouchHelper.attachToRecyclerView(listView)

        observeNowPlaying()
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