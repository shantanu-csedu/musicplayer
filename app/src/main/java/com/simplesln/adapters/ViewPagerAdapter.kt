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
            ALBUM -> createGroupListFragmentInstance(ALBUM,TYPE_ALBUM)
            ARTIST -> createGroupListFragmentInstance(ARTIST,TYPE_ARTIST)
            GENRE -> createGroupListFragmentInstance(GENRE,TYPE_GENRE)
            PLAYLIST -> createGroupListFragmentInstance(PLAYLIST,TYPE_PLAYLIST)
            SONGS -> createSongListFragmentInstance(SONGS,TYPE_ALL)
            LIBRARY -> createMusicLibrary()
            else -> null
        }
    }

    override fun getCount(): Int {
        return tabs.size;
    }
}