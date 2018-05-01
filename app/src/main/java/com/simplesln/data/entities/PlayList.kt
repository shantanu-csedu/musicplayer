package com.simplesln.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "media_playlist",
        indices = [(Index(value = arrayOf("name"), unique = true))])
class PlayList(name : String) {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
    var name : String = name
}