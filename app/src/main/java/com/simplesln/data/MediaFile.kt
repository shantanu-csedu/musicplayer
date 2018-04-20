package com.simplesln.data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "media_library")
class MediaFile {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
    var link : String
    var name : String
    var artist : String
    var genre : String
    var playList : String
    var album : String
    var nowPlaying : Boolean

    constructor(link: String, name: String, artist: String, genre: String, playList: String, album: String, nowPlaying : Boolean) {
        this.link = link
        this.name = name
        this.artist = artist
        this.genre = genre
        this.playList = playList
        this.album = album
        this.nowPlaying = nowPlaying
    }
}