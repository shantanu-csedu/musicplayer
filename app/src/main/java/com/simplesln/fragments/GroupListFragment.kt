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
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Toast
import com.simplesln.adapters.GroupListAdapter
import com.simplesln.adapters.PlaylistDialogListAdapter
import com.simplesln.data.Group
import com.simplesln.data.PlayList
import com.simplesln.data.entities.MediaFile
import com.simplesln.interfaces.OnIMenuItemClickListener
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R
import com.simplesln.simpleplayer.getDataProvider

class GroupListFragment : TitleFragment(), OnIMenuItemClickListener, AdapterView.OnItemClickListener {
    private var groupType = TYPE_ALBUM
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val group = mAdapter.values[position]
        val fragment = createSongListFragmentInstance(group.name, groupType)
        (activity as MainActivity).addDetailsFragment(group.name,fragment)
    }

    override fun onMenuClicked(anchorView: View,position : Int) {
        val popupMenu = PopupMenu(activity!!,anchorView)
        val groupItem = mAdapter.values[position]
        popupMenu.menuInflater.inflate(R.menu.menu_music_group,popupMenu.menu)
        if(groupType == TYPE_PLAYLIST) popupMenu.menu.removeItem(R.id.menu_add_playlist)
        else popupMenu.menu.removeItem(R.id.menu_delete)
        popupMenu.setOnMenuItemClickListener(object : OnIMenuItemClickListener, PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when(item?.itemId){
                    R.id.menu_delete ->{
                        Snackbar.make(listView,groupItem.name + " is removed", Snackbar.LENGTH_LONG)
                                .setAction("Undo", View.OnClickListener {
                                    mAdapter.values.add(position,groupItem)
                                    mAdapter.notifyItemInserted(position)
                                })
                                .addCallback(object : Snackbar.Callback(){
                                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                        super.onDismissed(transientBottomBar, event)
                                        if(event ==  Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_CONSECUTIVE) {
                                            getDataProvider(activity!!).removePlaylist(groupItem.name)
                                        }
                                    }
                                })
                                .show()
                        mAdapter.values.remove(groupItem)
                        mAdapter.notifyItemRemoved(position)

                    }
                    R.id.menu_play ->{
                        val group = mAdapter.values[position]
                        val mediaListLiveData = when(groupType){
                            TYPE_ARTIST ->
                                getDataProvider(activity!!).getMediaFilesByArtist(group.name)
                            TYPE_GENRE ->
                                getDataProvider(activity!!).getMediaFilesByGenre(group.name)
                            else ->
                                getDataProvider(activity!!).getMediaFilesByAlbum(group.name)
                        }

                        mediaListLiveData.observe(this@GroupListFragment,object : Observer<List<MediaFile>>{
                            override fun onChanged(t: List<MediaFile>?) {
                                mediaListLiveData.removeObserver(this)
                                if(t != null && t.size > 0) {
                                    val nowPlayMediaId = t[0].id
                                    val addNowPlayLiveData = getDataProvider(activity!!).addNowPlaying(t, true)
                                    addNowPlayLiveData.observe(this@GroupListFragment,object : Observer<Boolean>{
                                        override fun onChanged(t: Boolean?) {
                                            addNowPlayLiveData.removeObserver(this)
                                            getDataProvider(activity!!).setNowPlaying(nowPlayMediaId)
                                        }
                                    })
                                }
                            }
                        })
                        return true
                    }

                    R.id.menu_queue-> {
                        val group = mAdapter.values[position]
                        val mediaListLiveData = when(groupType){
                            TYPE_ARTIST ->
                                getDataProvider(activity!!).getMediaFilesByArtist(group.name)
                            TYPE_GENRE ->
                                getDataProvider(activity!!).getMediaFilesByGenre(group.name)
                            else ->
                                getDataProvider(activity!!).getMediaFilesByAlbum(group.name)
                        }
                        mediaListLiveData.observe(this@GroupListFragment,object : Observer<List<MediaFile>>{
                            override fun onChanged(t: List<MediaFile>?) {
                                mediaListLiveData.removeObserver(this)
                                if(t != null && t.isNotEmpty()) {
                                    getDataProvider(activity!!).addNowPlaying(t, false)
                                }
                            }
                        })
                        return true
                    }

                    R.id.menu_shuffle-> {
                        val group = mAdapter.values[position]
                        if(mAdapter.values.size > 0) {
                            val mediaListLiveData = when(groupType){
                                TYPE_ARTIST ->
                                    getDataProvider(activity!!).getMediaFilesByArtist(group.name)
                                TYPE_GENRE ->
                                    getDataProvider(activity!!).getMediaFilesByGenre(group.name)
                                else ->
                                    getDataProvider(activity!!).getMediaFilesByAlbum(group.name)
                            }

                            mediaListLiveData.observe(this@GroupListFragment,object : Observer<List<MediaFile>>{
                                override fun onChanged(t: List<MediaFile>?) {
                                    if(t?.size!! > 0) {
                                        mediaListLiveData.removeObserver(this)
                                        val shuffledList = t.shuffled()
                                        val addNowPlayLiveData = getDataProvider(activity!!).addNowPlaying(shuffledList, true)
                                        addNowPlayLiveData.observe(this@GroupListFragment, object : Observer<Boolean> {
                                            override fun onChanged(t: Boolean?) {
                                                addNowPlayLiveData.removeObserver(this)
                                                getDataProvider(activity!!).setNowPlaying(shuffledList[0].id)
                                            }
                                        })
                                    }
                                }
                            })

                        }
                    }

                    R.id.menu_add_playlist-> {
                        val group = mAdapter.values[position]
                        val mediaListLiveData = when(groupType){
                            TYPE_ARTIST ->
                                getDataProvider(activity!!).getMediaFilesByArtist(group.name)
                            TYPE_GENRE ->
                                getDataProvider(activity!!).getMediaFilesByGenre(group.name)
                            else ->
                                getDataProvider(activity!!).getMediaFilesByAlbum(group.name)
                        }
                        mediaListLiveData.observe(this@GroupListFragment,object : Observer<List<MediaFile>>{
                            override fun onChanged(t: List<MediaFile>?) {
                                mediaListLiveData.removeObserver(this)
                                if(t != null && t.isNotEmpty()) {
                                    val builder = AlertDialog.Builder(activity)
                                    val dialogAdapter = PlaylistDialogListAdapter(activity!!)
                                    val playlistLiveData = getDataProvider(activity!!).getPlayList()
                                    playlistLiveData.observe(this@GroupListFragment, Observer {
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
                                        getDataProvider(activity!!).addToPlayList(playlist.name,t)
                                        Toast.makeText(activity,"added to " + playlist.name,Toast.LENGTH_SHORT).show()
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
                                                getDataProvider(activity!!).addToPlayList(editText.text.toString(), t)
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
                            }
                        })
                        return true
                    }
                }
                return false
            }

            override fun onMenuClicked(anchorView: View, position: Int) {

            }
        })
        popupMenu.show()
    }

    lateinit var listView : RecyclerView
    lateinit var mAdapter : GroupListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments?.getInt(GROUP_TYPE) != null){
            groupType = arguments?.getInt(GROUP_TYPE)!!
        }
        mAdapter = GroupListAdapter(activity!!,this)
        mAdapter.setOnItemClickListener(this)

        observe()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album_list,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        listView.layoutManager = GridLayoutManager(activity,2)
        listView.adapter = mAdapter
    }

    private fun observe(){
        when(groupType){
            TYPE_ALBUM ->
                getDataProvider(activity!!).getAlbumList().observe(this, Observer {
                    if(it != null){
                        mAdapter.values.clear()
                        for(name in it){
                            mAdapter.values.add(Group(name))
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                })
            TYPE_ARTIST ->
                getDataProvider(activity!!).getArtistList().observe(this, Observer {
                    if(it != null){
                        mAdapter.values.clear()
                        for(name in it){
                            mAdapter.values.add(Group(name))
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                })

            TYPE_GENRE ->
                getDataProvider(activity!!).getGenreList().observe(this, Observer {
                    if(it != null){
                        mAdapter.values.clear()
                        for(name in it){
                            mAdapter.values.add(Group(name))
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                })

            TYPE_PLAYLIST ->
                getDataProvider(activity!!).getPlayList().observe(this, Observer {
                    if(it != null){
                        mAdapter.values.clear()
                        for(playList in it){
                            mAdapter.values.add(Group(playList.name))
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                })
        }

    }

}
private const val GROUP_TYPE = "group_type"
fun createGroupListFragmentInstance(title: String, type : Int) : GroupListFragment{
    val groupListFragment = GroupListFragment()
    val bundle = Bundle()
    bundle.putInt(GROUP_TYPE,type)
    bundle.putString(TITLE,title)
    groupListFragment.arguments = bundle
    return groupListFragment
}