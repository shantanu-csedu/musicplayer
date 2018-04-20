package com.simplesln.data

import android.arch.lifecycle.LiveData
import android.content.Context
import com.simplesln.interfaces.DataProvider

class RoomDataProvider(var context : Context) : DataProvider{
    private var db : MyDB?
    init {
        db = getInstance(context)
    }

    override fun getNowPlay(): LiveData<MediaFile> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNowPlayList(): LiveData<List<MediaFile>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNext(): LiveData<MediaFile> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPrev(): LiveData<MediaFile> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addMedia(index: Int, file: MediaFile) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addMedia(files: List<MediaFile>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAll() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}