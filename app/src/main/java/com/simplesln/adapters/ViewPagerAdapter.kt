package com.simplesln.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.simplesln.fragments.*

class ViewPagerAdapter(private val childCount : Int, fm: FragmentManager?) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? {
        return when(position){
            0 -> NowPlayingFragment()
            1 -> AlbumListFragment()
            2 -> ArtistListFragment()
            3 -> GenreListFragment()
            4 -> PlayListFragment()
            5 -> AlbumListFragment()
            else -> null
        }
    }

    override fun getCount(): Int {
        return childCount;
    }
}