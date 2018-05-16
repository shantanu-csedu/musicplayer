package com.simplesln.fragments

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplesln.adapters.PlayListAdapter
import com.simplesln.data.PlayList
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R
import com.simplesln.simpleplayer.getDataProvider

class PlayListFragment : Fragment(){
    lateinit var listView : RecyclerView
    lateinit var mAdapter : PlayListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = PlayListAdapter(activity!!)
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
        getDataProvider(activity!!).getPlayList().observe(this, Observer {
            if(it != null){
                mAdapter.values.clear()
                for(playlist in it){
                    mAdapter.values.add(PlayList(playlist))
                }
                mAdapter.notifyDataSetChanged()
            }
        })
    }
}