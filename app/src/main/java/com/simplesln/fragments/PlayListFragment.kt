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