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