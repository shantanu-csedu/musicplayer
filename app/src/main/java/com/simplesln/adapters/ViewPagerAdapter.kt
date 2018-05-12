package com.simplesln.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.simplesln.fragments.*
import com.simplesln.simpleplayer.*

class ViewPagerAdapter(private val tabs : Array<String>, fm: FragmentManager?) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? {
        return when(tabs[position]){
            NOW_PLAYING -> NowPlayingFragment()
            ALBUM -> createGroupListFragmentInstance(TYPE_ALBUM)
            ARTIST -> createGroupListFragmentInstance(TYPE_ARTIST)
            GENRE -> createGroupListFragmentInstance(TYPE_GENRE)
            PLAYLIST -> createGroupListFragmentInstance(TYPE_PLAYLIST)
            SONGS -> createSongListFragmentInstance(TYPE_ALL)
            else -> null
        }
    }

    override fun getCount(): Int {
        return tabs.size;
    }
}