package com.simplesln.fragments

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplesln.adapters.AlbumListAdapter
import com.simplesln.adapters.ArtistListAdapter
import com.simplesln.data.Album
import com.simplesln.data.Artist
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R

class ArtistListFragment : Fragment(){
    lateinit var listView : RecyclerView
    lateinit var mAdapter : ArtistListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = ArtistListAdapter(activity!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_artist_list,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        listView.layoutManager = GridLayoutManager(activity,2)
        listView.adapter = mAdapter
        observe()
    }

    private fun observe(){
        (activity as MainActivity).getDataProvider().getArtistList().observe(this, Observer {
            if(it != null){
                mAdapter.values.clear()
                for(name in it){
                    mAdapter.values.add(Artist(name))
                }
                mAdapter.notifyDataSetChanged()
            }
        })
    }
}