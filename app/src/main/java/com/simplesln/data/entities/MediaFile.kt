package com.simplesln.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "media_library")
class MediaFile {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
    var link : String
    var name : String
    var artist : String
    var genre : String
    var album : String
    var folder : String
    var year : String

    constructor(link: String, name: String, artist: String?, genre: String?, album: String?, folder: String, year: String?) {
        this.link = link
        this.name = name
        this.artist = if(artist == null) "" else artist
        this.genre = if(genre == null) "" else genre
        this.album = if(album == null) "" else album
        this.folder = folder
        this.year = if(year == null) "" else year
    }
}