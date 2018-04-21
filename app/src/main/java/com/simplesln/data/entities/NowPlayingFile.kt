package com.simplesln.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "media_now_playing",
        foreignKeys = [
            ForeignKey(
                    entity = MediaFile::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("media_file_id"),
                    onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index(value = arrayOf("media_file_id"), unique = true)]
)
class NowPlayingFile {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
    var media_file_id : Long
    var rank : Double
    var timestamp : Long
    var nowPlaying : Boolean

    constructor(media_file_id: Long, rank : Double, nowPlaying : Boolean) {
        this.media_file_id = media_file_id
        this.rank = rank
        this.timestamp = System.currentTimeMillis()
        this.nowPlaying = nowPlaying
    }
}