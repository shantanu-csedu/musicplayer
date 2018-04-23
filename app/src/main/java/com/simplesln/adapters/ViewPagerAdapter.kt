package com.simplesln.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.simplesln.fragments.MusicLibraryFragment
import com.simplesln.fragments.NowPlayingFragment

class ViewPagerAdapter(private val childCount : Int, fm: FragmentManager?) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? {
        return when(position){
            0 -> NowPlayingFragment()
            1 -> MusicLibraryFragment()
            else -> null
        }
    }

    override fun getCount(): Int {
        return childCount;
    }
}