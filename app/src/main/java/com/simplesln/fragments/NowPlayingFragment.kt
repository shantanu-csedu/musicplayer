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

package com.simplesln.fragments

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.*
import android.widget.AdapterView
import com.simplesln.adapters.NowPlayListAdapter
import com.simplesln.adapters.helper.ItemTouchHelperAdapter
import com.simplesln.adapters.helper.SimpleItemTouchHelperCallback
import com.simplesln.data.MediaFile
import com.simplesln.data.MediaPlayerState
import com.simplesln.data.STATE_PLAYING
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R
import com.simplesln.simpleplayer.getDataProvider

class NowPlayingFragment : Fragment(), AdapterView.OnItemClickListener, ItemTouchHelperAdapter {
    override fun onItemReleased() {
        val toPosition = mAdapter.moveToIndex
        mAdapter.moveToIndex = -1
        if(toPosition >= 0){
            val topPosition = toPosition - 1
            val bottomPosition = toPosition + 1
            if(topPosition >= 0 && bottomPosition < mAdapter.values.size){
                val rankLiveData = getDataProvider(activity!!).getRank(mAdapter.values[topPosition].id,mAdapter.values[bottomPosition].id)
                rankLiveData.observe(this,object : Observer<Double>{
                    override fun onChanged(rank: Double?) {
                        rankLiveData.removeObserver(this)
                        getDataProvider(activity!!).updateRank(mAdapter.values[toPosition].getEntity(),rank!!)
                    }
                })
            }
            else if(topPosition < 0 && bottomPosition < mAdapter.values.size){
                Log.e("bottom",mAdapter.values[bottomPosition].name)
                val rankLiveData = getDataProvider(activity!!).getRank(mAdapter.values[bottomPosition].id)
                rankLiveData.observe(this,object : Observer<Double>{
                    override fun onChanged(rank: Double?) {
                        rankLiveData.removeObserver(this)
                        getDataProvider(activity!!).updateRank(mAdapter.values[toPosition].getEntity(),rank!!-1)
                    }
                })
            }
            else if(topPosition >=0 && bottomPosition >= mAdapter.values.size){
                val rankLiveData = getDataProvider(activity!!).getRank(mAdapter.values[topPosition].id)
                rankLiveData.observe(this,object : Observer<Double>{
                    override fun onChanged(rank: Double?) {
                        rankLiveData.removeObserver(this)
                        getDataProvider(activity!!).updateRank(mAdapter.values[toPosition].getEntity(),rank!!+1)
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

        val item = mAdapter.values[position]
        getDataProvider(activity!!).getRank(item.id).observe(this, Observer {
            val itemRank = it!!
            Snackbar.make(listView,mAdapter.values[position].name + " is removed",Snackbar.LENGTH_LONG)
                    .setAction("Undo", View.OnClickListener {
                        getDataProvider(activity!!).addQueue(item.getEntity(),itemRank)
                    })
                    .show()
            mAdapter.onItemDismiss(position)
            handler.postDelayed({
                getDataProvider(activity!!).removeQueue(item.id)
            },300)

        })

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val mediaFile = mAdapter.values[position]
        getDataProvider(activity!!).setNowPlaying(mediaFile.id)
    }

    lateinit var listView : RecyclerView
    lateinit var mAdapter : NowPlayListAdapter
    lateinit var mImageTouchHelper : ItemTouchHelper
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mAdapter = NowPlayListAdapter(activity!!)
        mAdapter.setOnItemClickListener(this)
        observerQueue()
        observeMediaPlayerState()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_now_playing,container,false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_now_playing_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_shuffle->
                shuffle()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shuffle(){
        val shuffledList = mAdapter.values.shuffled()
        for((index,file) in shuffledList.withIndex()){
            getDataProvider(activity!!).updateRank(file.getEntity(), index.toDouble())
            if(index == 0){
                getDataProvider(activity!!).setNowPlaying(file.id)
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        listView.layoutManager = LinearLayoutManager(activity)
        listView.adapter = mAdapter

        val callback = SimpleItemTouchHelperCallback(this)
        mImageTouchHelper = ItemTouchHelper(callback)
        mImageTouchHelper.attachToRecyclerView(listView)

    }

    private fun observerQueue(){
        getDataProvider(activity!!).getQueue().observe(this, Observer {
            if (it != null  && !equal(mAdapter.values,it)) {
                mAdapter.values.clear()
                val lastState = (activity as MainActivity).liveMediaPlayerState.lastState
                if(lastState.mediaFile != null) {
                    Log.e("last state", lastState.mediaFile?.name)
                }
                else{
                    Log.e("last state", "media file null")
                }
                for(entity in it){
                    val mFile = MediaFile(entity)
                    if(lastState.mediaFile?.link.equals(mFile.link)){
                        mFile.playing = (lastState.state == STATE_PLAYING)
                    }
                    mAdapter.values.add(mFile)
                }
                mAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun equal(f1 : List<MediaFile>, f2 : List<com.simplesln.data.entities.MediaFile>) : Boolean{
        if(f1.size != f2.size) return false
        for((index,f) in f1.withIndex()){
            if(f != f1[index]) return false
        }
        return true
    }

    private fun observeMediaPlayerState(){
        (activity as MainActivity).liveMediaPlayerState.observe(this, Observer<MediaPlayerState> { it ->
            if(it != null){
                if(it.mediaFile != null) {
                    Log.e("state change", it.mediaFile?.name)
                }
                else{
                    Log.e("state change","media file null")
                }
                updatePlayingState(it)
            }
        })
    }

    private fun updatePlayingState(mediaPlayerState: MediaPlayerState){
        for(file in mAdapter.values){
            if(file.link.equals(mediaPlayerState.mediaFile?.link)){
                file.playing = (mediaPlayerState.state == STATE_PLAYING)
            }
            else{
                file.playing = false
            }
            mAdapter.notifyDataSetChanged()
        }
    }
}