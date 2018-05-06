package com.simplesln.fragments

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.os.Bundle
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

class GroupListFragment : Fragment(), OnIMenuItemClickListener, AdapterView.OnItemClickListener {
    private var groupType = TYPE_ALBUM
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val group = mAdapter.values[position]
        val fragment = createSongListFragmentInstance(groupType,group.name)
        (activity as MainActivity).addDetailsFragment(group.name,fragment)
    }

    override fun onMenuClicked(anchorView: View,position : Int) {
        val popupMenu = PopupMenu(activity!!,anchorView)
        popupMenu.menuInflater.inflate(R.menu.menu_music_group,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(object : OnIMenuItemClickListener, PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                if(item?.itemId == R.id.menu_play){
                    val album = mAdapter.values[position]
                    val mediaListLiveData = (activity as MainActivity).getDataProvider().getMediaFilesByAlbum(album.name)
                    mediaListLiveData.observe(this@GroupListFragment,object : Observer<List<MediaFile>>{
                        override fun onChanged(t: List<MediaFile>?) {
                            mediaListLiveData.removeObserver(this)
                            if(t != null && t.size > 0) {
                                val nowPlayMediaId = t[0].id
                                val addNowPlayLiveData = (activity as MainActivity).getDataProvider().addNowPlaying(t, true)
                                addNowPlayLiveData.observe(this@GroupListFragment,object : Observer<Boolean>{
                                    override fun onChanged(t: Boolean?) {
                                        addNowPlayLiveData.removeObserver(this)
                                        (activity as MainActivity).getDataProvider().setNowPlaying(nowPlayMediaId)
                                    }
                                })
                            }
                        }
                    })
                    return true
                }
                else if(item?.itemId == R.id.menu_queue){
                    val album = mAdapter.values[position]
                    val mediaListLiveData = (activity as MainActivity).getDataProvider().getMediaFilesByAlbum(album.name)
                    mediaListLiveData.observe(this@GroupListFragment,object : Observer<List<MediaFile>>{
                        override fun onChanged(t: List<MediaFile>?) {
                            mediaListLiveData.removeObserver(this)
                            if(t != null && t.isNotEmpty()) {
                                (activity as MainActivity).getDataProvider().addNowPlaying(t, false)
                            }
                        }
                    })
                    return true
                }
                else if(item?.itemId == R.id.menu_add_playlist){
                    val album = mAdapter.values[position]
                    val mediaListLiveData = (activity as MainActivity).getDataProvider().getMediaFilesByAlbum(album.name)
                    mediaListLiveData.observe(this@GroupListFragment,object : Observer<List<MediaFile>>{
                        override fun onChanged(t: List<MediaFile>?) {
                            mediaListLiveData.removeObserver(this)
                            if(t != null && t.isNotEmpty()) {
                                val builder = AlertDialog.Builder(activity)
                                val dialogAdapter = PlaylistDialogListAdapter(activity!!)
                                val playlistLiveData = (activity as MainActivity).getDataProvider().getPlayList()
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
                                    (activity as MainActivity).getDataProvider().addToPlayList(playlist.name,t)
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
                                            (activity as MainActivity).getDataProvider().addToPlayList(editText.text.toString(), t)
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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album_list,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        listView.layoutManager = GridLayoutManager(activity,2)
        listView.adapter = mAdapter
        observe()
    }

    private fun observe(){
        when(groupType){
            TYPE_ALBUM ->
                (activity as MainActivity).getDataProvider().getAlbumList().observe(this, Observer {
                    if(it != null){
                        mAdapter.values.clear()
                        for(name in it){
                            mAdapter.values.add(Group(name))
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                })
            TYPE_ARTIST ->
                (activity as MainActivity).getDataProvider().getArtistList().observe(this, Observer {
                    if(it != null){
                        mAdapter.values.clear()
                        for(name in it){
                            mAdapter.values.add(Group(name))
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                })
            TYPE_GENRE ->
                (activity as MainActivity).getDataProvider().getGenreList().observe(this, Observer {
                    if(it != null){
                        mAdapter.values.clear()
                        for(name in it){
                            mAdapter.values.add(Group(name))
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                })
        }

    }

}
private const val GROUP_TYPE = "group_type"
fun createGroupListFragmentInstance(type : Int) : GroupListFragment{
    val groupListFragment = GroupListFragment()
    val bundle = Bundle()
    bundle.putInt(GROUP_TYPE,type)
    groupListFragment.arguments = bundle
    return groupListFragment
}