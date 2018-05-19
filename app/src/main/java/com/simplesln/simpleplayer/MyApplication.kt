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

package com.simplesln.simpleplayer

import android.app.Application
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.simplesln.repositories.PrefDataProvider
import com.simplesln.repositories.RoomDataProvider
import com.simplesln.interfaces.DataProvider
import com.simplesln.interfaces.Player
import com.simplesln.players.NativeMediaPlayer

class MyApplication : Application(){

    lateinit var pref : PrefDataProvider
    lateinit var dataProvider: DataProvider

    lateinit var sAnalytics: GoogleAnalytics

    override fun onCreate() {
        super.onCreate()
        pref = PrefDataProvider(this)
        dataProvider = RoomDataProvider(this)
        sAnalytics = GoogleAnalytics.getInstance(this);
        sAnalytics.setLocalDispatchPeriod(1800);
        val tracker = sAnalytics.newTracker(R.xml.ecommerce_tracker)
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
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

fun getDefaultTracker(context: Context) : Tracker{
    return (context.applicationContext as MyApplication).sAnalytics.newTracker(R.xml.ecommerce_tracker)
}