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

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Toast
import com.simplesln.adapters.PlaylistDialogListAdapter
import com.simplesln.adapters.SongListAdapter
import com.simplesln.adapters.helper.ItemTouchHelperAdapter
import com.simplesln.adapters.helper.SimpleItemTouchHelperCallback
import com.simplesln.data.MediaPlayerState
import com.simplesln.data.PlayList
import com.simplesln.data.STATE_PLAYING
import com.simplesln.data.entities.MediaFile
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R
import com.simplesln.simpleplayer.getDataProvider
import java.util.*
import kotlin.collections.ArrayList

class SongListFragment : TitleFragment(), AdapterView.OnItemClickListener, ItemTouchHelperAdapter {
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        mAdapter.onItemMove(fromPosition,toPosition)
    }

    override fun onItemDismiss(position: Int) {
        val item = mAdapter.values[position]
        Snackbar.make(listView,mAdapter.values[position].name + " is removed",Snackbar.LENGTH_LONG)
                .setAction("Undo", View.OnClickListener {
                    getDataProvider(activity!!).undeleteMedia(item.id)
                })
                .show()
        mAdapter.onItemDismiss(position)
        handler.postDelayed({
            getDataProvider(activity!!).deleteMedia(item.id)
        },300)
    }

    override fun onItemReleased() {
    }

    private lateinit var mAdapter : SongListAdapter
    private lateinit var listView : RecyclerView
    private var groupType = TYPE_ALBUM
    private var groupName = ""
    private var addedToNowPlayList = false
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mAdapter = SongListAdapter(activity!!)
        mAdapter.setOnItemClickListener(this)
        if(arguments != null) {
            if(arguments!!.get(GROUP_TYPE) != null) {
                groupType = arguments!!.getInt(GROUP_TYPE)
                groupName = arguments!!.getString(GROUP_NAME)
            }
        }
        observe()
        observeMediaPlayerState()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_song_list,container,false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_song_list_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        if(groupType == TYPE_PLAYLIST) menu?.removeItem(R.id.menu_add_playlist)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_shuffle ->
                if(mAdapter.values.size > 0) {
                    val shuffledList = mAdapter.values.shuffled()
                    val mediaFileList = ArrayList<MediaFile>()
                    for(file in shuffledList){
                        mediaFileList.add(file.getEntity())
                    }
                    val addNowPlayLiveData = getDataProvider(activity!!).addQueue(mediaFileList, true)
                    addNowPlayLiveData.observe(this,object : Observer<Boolean>{
                        override fun onChanged(t: Boolean?) {
                            addNowPlayLiveData.removeObserver(this)
                            getDataProvider(activity!!).setNowPlaying(shuffledList[0].id)
                        }
                    })
                }
            R.id.menu_add_queue ->
                if(mAdapter.values.size > 0) {
                    val mediaFileList = ArrayList<MediaFile>()
                    for(file in mAdapter.values){
                        mediaFileList.add(file.getEntity())
                    }
                    getDataProvider(activity!!).addQueue(mediaFileList, false)
                }
            R.id.menu_add_playlist ->
                if(mAdapter.values.size > 0) {
                    val mediaFileList = ArrayList<MediaFile>()
                    for(file in mAdapter.values){
                        mediaFileList.add(file.getEntity())
                    }
                    addToPlaylist(mediaFileList)
                }
        }
        return super.onOptionsItemSelected(item)
    }

    private lateinit var mImageTouchHelper: ItemTouchHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        listView.layoutManager = LinearLayoutManager(activity)
        listView.adapter = mAdapter
        val callback = SimpleItemTouchHelperCallback(this)
        mImageTouchHelper = ItemTouchHelper(callback)
        mImageTouchHelper.attachToRecyclerView(listView)
    }

    private fun observe(){
        when(groupType){
            TYPE_ALBUM -> getDataProvider(activity!!).getMediaFilesByAlbum(groupName).observe(this,mediaFileObserver)
            TYPE_ARTIST -> getDataProvider(activity!!).getMediaFilesByArtist(groupName).observe(this,mediaFileObserver)
            TYPE_GENRE -> getDataProvider(activity!!).getMediaFilesByGenre(groupName).observe(this,mediaFileObserver)
            TYPE_PLAYLIST -> getDataProvider(activity!!).getMediaFilesByPlaylist(groupName).observe(this,mediaFileObserver)
            TYPE_ALL -> getDataProvider(activity!!).getMediaFiles().observe(this,mediaFileObserver)
        }
    }

    private val mediaFileObserver =  Observer<List<MediaFile>>{
        addedToNowPlayList = false
        mAdapter.values.clear()
        if(it != null){
            val lastState = (activity as MainActivity).liveMediaPlayerState.lastState
            for(entity in it){
                val mFile = com.simplesln.data.MediaFile(entity)
                if(lastState.mediaFile?.link.equals(mFile.link)){
                    mFile.playing = (lastState.state == STATE_PLAYING)
                }
                mAdapter.values.add(mFile)
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val popupMenu = PopupMenu(activity!!,view!!)
        popupMenu.menuInflater.inflate(R.menu.menu_song_item,popupMenu.menu)
        if(groupType == TYPE_PLAYLIST) popupMenu.menu.removeItem(R.id.menu_add_playlist)
        popupMenu.setOnMenuItemClickListener { item ->
            val mediaFile = mAdapter.values[position]
            when(item?.itemId){
                R.id.menu_play_now ->{
                    if(!addedToNowPlayList) {
                        val mediaFileList = ArrayList<MediaFile>()
                        for(file in mAdapter.values){
                            mediaFileList.add(file.getEntity())
                        }
                        val nowPlayingLiveData = getDataProvider(activity!!).addQueue(mediaFileList, true)
                        nowPlayingLiveData.observe(this, object : Observer<Boolean> {
                            override fun onChanged(it: Boolean?) {
                                if (it == true) {
                                    nowPlayingLiveData.removeObserver(this)
                                    getDataProvider(activity!!).setNowPlaying(mediaFile.id)
                                    addedToNowPlayList = true
                                }
                            }
                        })
                    }
                    else{
                        getDataProvider(activity!!).setNowPlaying(mediaFile.id)
                    }
                }

                R.id.menu_play_next ->
                    getDataProvider(activity!!).setNext(mediaFile.id)
                R.id.menu_add_queue ->
                    getDataProvider(activity!!).addQueue(Arrays.asList(mediaFile.getEntity()),false)
                R.id.menu_add_playlist ->
                    addToPlaylist(Arrays.asList(mediaFile.getEntity()))
            }
            true
        }
        popupMenu.show()
    }

    private fun addToPlaylist(values : List<MediaFile>){
        val builder = AlertDialog.Builder(activity)
        val dialogAdapter = PlaylistDialogListAdapter(activity!!)
        val playlistLiveData = getDataProvider(activity!!).getPlayList()
        playlistLiveData.observe(this@SongListFragment, Observer {
            if(it != null){
                for(playList in it){
                    dialogAdapter.add(PlayList(playList))
                }
                dialogAdapter.notifyDataSetChanged()
            }
        })
        builder.setTitle("Playlist")
        builder.setAdapter(dialogAdapter) {
            dialog, which ->
            dialog?.dismiss()
            val playlist = dialogAdapter.getItem(which)
            getDataProvider(activity!!).addToPlayList(playlist.name,values)
            Toast.makeText(activity,"added to " + playlist.name, Toast.LENGTH_SHORT).show()
        }
        builder.setPositiveButton("Create"){
            dialog, _ ->
            dialog.dismiss()
            val createDialog = AlertDialog.Builder(activity)
            createDialog.setTitle("Create Playlist")
            val inflatedView = LayoutInflater.from(activity).inflate(R.layout.dialog_playlist_entry,null)
            createDialog.setView(inflatedView)
            val editText : EditText = inflatedView.findViewById(R.id.playlistName)
            createDialog.setPositiveButton("Okay"){
                dialog, _ ->
                dialog.dismiss()
                if(editText.text.toString().isNotEmpty()) {
                    getDataProvider(activity!!).addToPlayList(editText.text.toString(), values)
                    Toast.makeText(activity, "added to " + editText.text.toString(), Toast.LENGTH_SHORT).show()
                }
            }
            createDialog.setNegativeButton("Cancel"){
                dialog, _ ->
                dialog.dismiss()
            }
            createDialog.create().show()
        }
        builder.setNegativeButton("Cancel"){
            dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
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
const val TYPE_ALBUM = 1
const val TYPE_ARTIST = 2
const val TYPE_GENRE = 3
const val TYPE_PLAYLIST = 4
const val TYPE_ALL = 5
private const val GROUP_TYPE = "group_type"
private const val GROUP_NAME = "group_name"

fun createSongListFragmentInstance(title : String ,groupName : String, type : Int) : SongListFragment{
    val bundle = Bundle()
    bundle.putInt(GROUP_TYPE,type)
    bundle.putString(TITLE,title)
    bundle.putString(GROUP_NAME,groupName)
    val songListFragment = SongListFragment()
    songListFragment.arguments = bundle
    return songListFragment
}