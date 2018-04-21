package com.simplesln.data

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