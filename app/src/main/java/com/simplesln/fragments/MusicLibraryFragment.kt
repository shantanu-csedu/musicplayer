package com.simplesln.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.simplesln.adapters.CategoryListAdapter
import com.simplesln.data.Category
import com.simplesln.simpleplayer.*


class MusicLibraryFragment : Fragment(), AdapterView.OnItemClickListener {
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(mAdapter.values[position].name){
            ALBUM->{
                (activity as MainActivity).addDetailsFragment(ALBUM,createGroupListFragmentInstance(ALBUM,TYPE_ALBUM))
            }
            ARTIST->{
                (activity as MainActivity).addDetailsFragment(ARTIST,createGroupListFragmentInstance(ARTIST,TYPE_ARTIST))
            }
            PLAYLIST->{
                (activity as MainActivity).addDetailsFragment(PLAYLIST,createGroupListFragmentInstance(PLAYLIST,TYPE_PLAYLIST))
            }
            GENRE->{
                (activity as MainActivity).addDetailsFragment(GENRE,createGroupListFragmentInstance(GENRE, TYPE_GENRE))
            }
            SONGS->{
                (activity as MainActivity).addDetailsFragment(SONGS, createSongListFragmentInstance(SONGS,TYPE_ALL))
            }
        }
    }

    private lateinit var listView : RecyclerView

    private lateinit var mAdapter : CategoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = CategoryListAdapter(activity!!)

        mAdapter.values.add(Category(R.mipmap.ic_music_album, ALBUM))
        mAdapter.values.add(Category(R.mipmap.ic_music_artist, ARTIST))
        mAdapter.values.add(Category(R.mipmap.ic_music_genre, GENRE))
        mAdapter.values.add(Category(R.mipmap.ic_music_playlist, PLAYLIST))
        mAdapter.values.add(Category(R.mipmap.ic_songs, SONGS))

        mAdapter.setOnItemClickListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_music_library,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        listView.layoutManager = GridLayoutManager(activity,2)
        listView.adapter = mAdapter
    }
}

fun createMusicLibrary() : MusicLibraryFragment{
    return MusicLibraryFragment()
}