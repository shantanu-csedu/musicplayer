package com.simplesln.fragments

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
import com.simplesln.adapters.AlbumListAdapter
import com.simplesln.data.Album
import com.simplesln.data.entities.MediaFile
import com.simplesln.interfaces.OnIMenuItemClickListener
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R

class AlbumListFragment : Fragment(), OnIMenuItemClickListener, AdapterView.OnItemClickListener {

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val album = mAdapter.values[position]
        val fragment = createInstance(TYPE_ALBUM,album.name)
        (activity as MainActivity).addDetailsFragment(album.name,fragment)
    }

    override fun onMenuClicked(anchorView: View,position : Int) {
        val popupMenu = PopupMenu(activity!!,anchorView)
        popupMenu.menuInflater.inflate(R.menu.menu_music_group,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(object : OnIMenuItemClickListener, PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                if(item?.itemId == R.id.menu_play){
                    val album = mAdapter.values[position]
                    val mediaListLiveData = (activity as MainActivity).getDataProvider().getMediaFilesByAlbum(album.name)
                    mediaListLiveData.observe(this@AlbumListFragment,object : Observer<List<MediaFile>>{
                        override fun onChanged(t: List<MediaFile>?) {
                            mediaListLiveData.removeObserver(this)
                            if(t != null && t.size > 0) {
                                val nowPlayMediaId = t[0].id
                                val addNowPlayLiveData = (activity as MainActivity).getDataProvider().addNowPlaying(t, true)
                                addNowPlayLiveData.observe(this@AlbumListFragment,object : Observer<Boolean>{
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
                    mediaListLiveData.observe(this@AlbumListFragment,object : Observer<List<MediaFile>>{
                        override fun onChanged(t: List<MediaFile>?) {
                            mediaListLiveData.removeObserver(this)
                            if(t != null && t.isNotEmpty()) {
                                (activity as MainActivity).getDataProvider().addNowPlaying(t, false)
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
    lateinit var mAdapter : AlbumListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = AlbumListAdapter(activity!!,this)
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
        (activity as MainActivity).getDataProvider().getAlbumList().observe(this, Observer {
            if(it != null){
                mAdapter.values.clear()
                for(name in it){
                    mAdapter.values.add(Album(name))
                }
                mAdapter.notifyDataSetChanged()
            }
        })
    }
}
