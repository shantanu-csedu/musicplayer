package com.simplesln.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.simplesln.fragments.*

class ViewPagerAdapter(private val childCount : Int, fm: FragmentManager?) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? {
        return when(position){
            0 -> NowPlayingFragment()
            1 -> createGroupListFragmentInstance(TYPE_ALBUM)
            2 -> createGroupListFragmentInstance(TYPE_ARTIST)
            3 -> createGroupListFragmentInstance(TYPE_GENRE)
            4 -> createGroupListFragmentInstance(TYPE_PLAYLIST)
            5 -> createSongListFragmentInstance(TYPE_ALL)
            else -> null
        }
    }

    override fun getCount(): Int {
        return childCount;
    }
}