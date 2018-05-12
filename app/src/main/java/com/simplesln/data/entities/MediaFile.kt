package com.simplesln.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "media_library")
class MediaFile(link: String, name: String, duration: Int, artist: String?, genre: String?, album: String?, folder: String, year: String?,repeatCount : Int = 1) {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
    var link : String = link
    var name : String = name
    var duration : Int = duration
    var artist : String = if(artist == null || artist.isEmpty()) "unknown" else artist
    var genre : String = if(genre == null || genre.isEmpty()) "unknown" else genre
    var album : String = if(album == null || album.isEmpty()) "unknown" else album
    var folder : String = folder
    var year : String = if(year == null || year.isEmpty()) "unknown" else year
    var repeatCount : Int = repeatCount
    var art : String = ""
}