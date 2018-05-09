package com.simplesln.fragments

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Toast
import com.simplesln.adapters.PlaylistDialogListAdapter
import com.simplesln.adapters.SongListAdapter
import com.simplesln.data.PlayList
import com.simplesln.data.entities.MediaFile
import com.simplesln.interfaces.OnIMenuItemClickListener
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R
import java.util.*

class SongListFragment : Fragment(), AdapterView.OnItemClickListener, OnIMenuItemClickListener {
    override fun onMenuClicked(anchorView: View, position: Int) {
        val popupMenu = PopupMenu(activity!!,anchorView)
        popupMenu.menuInflater.inflate(R.menu.menu_song_item,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            val mediaFile = mAdapter.values[position]
            when(item?.itemId){
                R.id.menu_play_next ->
                    (activity as MainActivity).getDataProvider().setNext(mediaFile.id)
                R.id.menu_add_queue ->
                    (activity as MainActivity).getDataProvider().addNowPlaying(Arrays.asList(mediaFile),false)
                R.id.menu_add_playlist ->
                    addToPlaylist(Arrays.asList(mediaFile))
            }
            true
        }
        popupMenu.show()
    }

    private lateinit var mAdapter : SongListAdapter
    private lateinit var listView : RecyclerView
    private var groupType = TYPE_ALBUM
    private var groupName = ""
    private var addedToNowPlayList = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mAdapter = SongListAdapter(activity!!,this)
        mAdapter.setOnItemClickListener(this)
        if(arguments != null) {
            if(arguments!!.get(GROUP_TYPE) != null && arguments!!.get(GROUP_NAME) != null) {
                groupType = arguments!!.getInt(GROUP_TYPE)
                groupName = arguments!!.getString(GROUP_NAME)
            }
        }
        observe()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_song_list,container,false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_song_list_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_shuffle ->
                if(mAdapter.values.size > 0) {
                    val shuffledList = mAdapter.values.shuffled()
                    val addNowPlayLiveData = (activity as MainActivity).getDataProvider().addNowPlaying(shuffledList, true)
                    addNowPlayLiveData.observe(this,object : Observer<Boolean>{
                        override fun onChanged(t: Boolean?) {
                            addNowPlayLiveData.removeObserver(this)
                            (activity as MainActivity).getDataProvider().setNowPlaying(shuffledList[0].id)
                        }
                    })
                }
            R.id.menu_add_queue ->
                if(mAdapter.values.size > 0) {
                    (activity as MainActivity).getDataProvider().addNowPlaying(mAdapter.values, false)
                }
            R.id.menu_add_playlist ->
                if(mAdapter.values.size > 0) {
                    addToPlaylist(mAdapter.values)
                }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        listView.layoutManager = LinearLayoutManager(activity)
        listView.adapter = mAdapter
    }

    private fun observe(){
        when(groupType){
            TYPE_ALBUM -> (activity as MainActivity).getDataProvider().getMediaFilesByAlbum(groupName).observe(this,mediaFileObserver)
            TYPE_ARTIST -> (activity as MainActivity).getDataProvider().getMediaFilesByArtist(groupName).observe(this,mediaFileObserver)
            TYPE_GENRE -> (activity as MainActivity).getDataProvider().getMediaFilesByGenre(groupName).observe(this,mediaFileObserver)
            TYPE_ALL -> (activity as MainActivity).getDataProvider().getMediaFiles().observe(this,mediaFileObserver)
        }
    }

    private val mediaFileObserver =  Observer<List<MediaFile>>{
        addedToNowPlayList = false
        mAdapter.values.clear()
        mAdapter.values.addAll(it!!)
        mAdapter.notifyDataSetChanged()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val mediaFile = mAdapter.values[position]
        if(!addedToNowPlayList) {
            val nowPlayingLiveData = (activity as MainActivity).getDataProvider().addNowPlaying(mAdapter.values, true)
            nowPlayingLiveData.observe(this, object : Observer<Boolean> {
                override fun onChanged(it: Boolean?) {
                    if (it == true) {
                        nowPlayingLiveData.removeObserver(this)
                        (activity as MainActivity).getDataProvider().setNowPlaying(mediaFile.id)
                        addedToNowPlayList = true
                    }
                }
            })
        }
        else{
            (activity as MainActivity).getDataProvider().setNowPlaying(mediaFile.id)
        }
    }

    private fun addToPlaylist(values : List<MediaFile>){
        val builder = AlertDialog.Builder(activity)
        val dialogAdapter = PlaylistDialogListAdapter(activity!!)
        val playlistLiveData = (activity as MainActivity).getDataProvider().getPlayList()
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
            (activity as MainActivity).getDataProvider().addToPlayList(playlist.name,values)
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
                    (activity as MainActivity).getDataProvider().addToPlayList(editText.text.toString(), values)
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
const val TYPE_ALBUM = 1
const val TYPE_ARTIST = 2
const val TYPE_GENRE = 3
const val TYPE_ALL = 4
private const val GROUP_TYPE = "group_type"
private const val GROUP_NAME = "group_name"

fun createSongListFragmentInstance(type : Int, name : String = "") : SongListFragment{
    val bundle = Bundle()
    bundle.putInt(GROUP_TYPE,type)
    bundle.putString(GROUP_NAME,name)
    val songListFragment = SongListFragment()
    songListFragment.arguments = bundle
    return songListFragment
}