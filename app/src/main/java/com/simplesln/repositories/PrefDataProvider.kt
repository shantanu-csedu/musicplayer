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

package com.simplesln.repositories

import android.content.Context
import android.content.SharedPreferences

class PrefDataProvider(context: Context) {
    private var pref : SharedPreferences
    init {
        pref = context.getSharedPreferences("my_pref",0)
    }

    fun isScanRunning() : Boolean{
        return pref.getBoolean("scan_running",false);
    }

    fun scanRunning(state : Boolean){
        pref.edit().putBoolean("scan_running",state).commit()
    }

    fun everIndexed() : Boolean{
        return pref.getBoolean("ever_index_run",false)
    }

    fun everIndexed(state : Boolean){
        pref.edit().putBoolean("ever_index_run",state).commit()
    }
}