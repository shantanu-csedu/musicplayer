package com.simplesln.simpleplayer

import android.app.Application
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import com.simplesln.repositories.PrefDataProvider
import com.simplesln.repositories.RoomDataProvider
import com.simplesln.interfaces.DataProvider
import com.simplesln.interfaces.Player
import com.simplesln.players.NativeMediaPlayer

class MyApplication : Application(){

    lateinit var pref : PrefDataProvider
    lateinit var dataProvider: DataProvider

    override fun onCreate() {
        super.onCreate()
        pref = PrefDataProvider(this)
        dataProvider = RoomDataProvider(this)
    }
}

fun getPref(context : Context): PrefDataProvider {
    return (context.applicationContext as MyApplication).pref
}

fun getDataProvider(context: Context) : DataProvider{
    return (context.applicationContext as MyApplication).dataProvider
}

fun createPlayer(context: Context,lifecycleOwner: LifecycleOwner):Player{
    return NativeMediaPlayer(context,lifecycleOwner, getDataProvider(context))
}