package com.simplesln.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.text.TextUtils

@Entity(tableName = "media_library")
class MediaFile {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
    var link : String
    var name : String
    var duration : Long
    var artist : String
    var genre : String
    var album : String
    var folder : String
    var year : String

    constructor(link: String, name: String, duration: Long, artist: String?, genre: String?, album: String?, folder: String, year: String?) {
        this.link = link
        this.name = name
        this.duration = duration
        this.artist = if(TextUtils.isEmpty(artist)) "unknown artist" else artist!!
        this.genre = if(TextUtils.isEmpty(genre)) "unknown genre" else genre!!
        this.album = if(TextUtils.isEmpty(album)) "unknown album" else album!!
        this.folder = folder
        this.year = if(year == null) "unknown year" else year
    }
}