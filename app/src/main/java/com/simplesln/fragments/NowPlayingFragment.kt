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
                        (activity as MainActivity).getDataProvider().updateRank(mAdapter.values[toPosition].getEntity(),rank!!)
                    }
                })
            }
            else if(topPosition < 0 && bottomPosition < mAdapter.values.size){
                Log.e("bottom",mAdapter.values[bottomPosition].name)
                val rankLiveData = (activity as MainActivity).getDataProvider().getRank(mAdapter.values[bottomPosition].id,true)
                rankLiveData.observe(this,object : Observer<Double>{
                    override fun onChanged(rank: Double?) {
                        rankLiveData.removeObserver(this)
                        (activity as MainActivity).getDataProvider().updateRank(mAdapter.values[toPosition].getEntity(),rank!!)
                    }
                })
            }
            else if(topPosition >=0 && bottomPosition >= mAdapter.values.size){
                val rankLiveData = (activity as MainActivity).getDataProvider().getRank(mAdapter.values[topPosition].id,false)
                rankLiveData.observe(this,object : Observer<Double>{
                    override fun onChanged(rank: Double?) {
                        rankLiveData.removeObserver(this)
                        (activity as MainActivity).getDataProvider().updateRank(mAdapter.values[toPosition].getEntity(),rank!!)
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
        Snackbar.make(listView,mAdapter.values[position].name + " is removed",Snackbar.LENGTH_LONG)
                .setAction("Undo", View.OnClickListener {
                    mAdapter.values.add(position,item)
                    mAdapter.notifyItemInserted(position)
                })
                .addCallback(object : Snackbar.Callback(){
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        if(event ==  Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_CONSECUTIVE) {
                            (activity as MainActivity).getDataProvider().removeQueue(item.id)
                        }
                    }
                })
                .show()
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
        setHasOptionsMenu(true)
        mAdapter = NowPlayListAdapter(activity!!,(activity as MainActivity).getDataProvider())
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
            (activity as MainActivity).getDataProvider().updateRank(file.getEntity(), index.toDouble())
            if(index == 0){
                (activity as MainActivity).getDataProvider().setNowPlaying(file.id)
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
        (activity as MainActivity).getDataProvider().getQueue().observe(this, Observer {
            mAdapter.values.clear()
            if (it != null) {
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
            }
            mAdapter.notifyDataSetChanged()
        })
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