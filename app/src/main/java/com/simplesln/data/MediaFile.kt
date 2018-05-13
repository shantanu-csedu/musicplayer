package com.simplesln.data

import com.simplesln.data.entities.MediaFile

class MediaFile(entity : MediaFile,playing : Boolean = false) {
    var id : Long = entity.id
    var link : String = entity.link
    var name : String = entity.name
    var duration : Int = entity.duration
    var artist : String = entity.artist
    var genre : String = entity.genre
    var album : String = entity.album
    var folder : String = entity.folder
    var year : String = entity.year
    var repeatCount : Int = entity.repeatCount
    var art : String = entity.art
    var playing = playing

    fun getEntity() : MediaFile{
        return MediaFile(link,name,duration,artist,genre,album,folder,year,repeatCount,art,id)
    }
}