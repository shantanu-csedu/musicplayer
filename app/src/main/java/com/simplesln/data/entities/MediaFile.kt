package com.simplesln.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

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
        this.artist = if(artist == null) "unknown artist" else artist
        this.genre = if(genre == null) "unknown genre" else genre
        this.album = if(album == null) "unknown album" else album
        this.folder = folder
        this.year = if(year == null) "unknown year" else year
    }
}