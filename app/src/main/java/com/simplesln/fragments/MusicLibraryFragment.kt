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
import com.simplesln.simpleplayer.R


class MusicLibraryFragment : Fragment(), AdapterView.OnItemClickListener {
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val category = mAdapter.values[position]

    }

    private lateinit var listView : RecyclerView

    private lateinit var mAdapter : CategoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = CategoryListAdapter(activity!!)

        mAdapter.values.add(Category(R.mipmap.ic_music_album,"Albums"))
        mAdapter.values.add(Category(R.mipmap.ic_music_artist,"Artists"))
        mAdapter.values.add(Category(R.mipmap.ic_music_playlist,"Playlist"))
        mAdapter.values.add(Category(R.mipmap.ic_songs,"Songs"))

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